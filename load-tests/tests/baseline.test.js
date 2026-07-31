import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// Custom metrics
const loginDuration = new Trend('login_duration');
const firestoreWriteDuration = new Trend('firestore_write_duration');
const geminiApiDuration = new Trend('gemini_api_duration');
const errorCounter = new Counter('errors');

export const options = {
    stages: [
        { duration: '10s', target: 100 }, // Ramp-up to 100 users
        { duration: '1m', target: 100 },  // Stay at 100 users for 1 minute
        { duration: '10s', target: 0 },   // Ramp-down
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'], // http errors should be less than 1%
        http_req_duration: ['p(95)<1000'], // 95% of requests should be below 1000ms
    },
};

const API_KEY = __ENV.FIREBASE_API_KEY || 'AIzaSyAWgtargZfuuVKFaFTeHoKH3nToGSD6H_Y';
const PROJECT_ID = __ENV.FIREBASE_PROJECT_ID || 'netadaptive-bf351';
const GEMINI_KEY = __ENV.GEMINI_API_KEY || 'AIzaSyDLVNv8gK3if5iTQRMf0W4ezbmuMVXPxbI';

export default function () {
    // 1. Authentication Simulation
    const loginUrl = `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`;
    const loginPayload = JSON.stringify({
        email: 'test@example.com',
        password: 'password123',
        returnSecureToken: true,
    });
    const loginParams = { headers: { 'Content-Type': 'application/json' } };

    const loginRes = http.post(loginUrl, loginPayload, loginParams);
    check(loginRes, {
        'login successful': (r) => r.status === 200,
    }) || errorCounter.add(1);
    loginDuration.add(loginRes.timings.duration);

    if (loginRes.status === 200) {
        const idToken = loginRes.json().idToken;
        const localId = loginRes.json().localId;

        // 2. Firestore Analytics Sync Simulation (GET then PATCH)
        const firestoreUrl = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/users/${localId}/analytics/current`;
        const firestoreParams = {
            headers: {
                'Authorization': `Bearer ${idToken}`,
                'Content-Type': 'application/json',
            },
        };

        // Simulated Read
        const getRes = http.get(firestoreUrl, firestoreParams);
        check(getRes, {
            'firestore read success': (r) => r.status === 200 || r.status === 404,
        }) || errorCounter.add(1);

        sleep(1);

        // Simulated Write (PATCH)
        const updatePayload = JSON.stringify({
            fields: {
                dailyUsageMB: { doubleValue: Math.random() * 500 },
                lastUpdated: { timestampValue: new Date().toISOString() },
            },
        });
        const patchRes = http.patch(`${firestoreUrl}?updateMask.fieldPaths=dailyUsageMB&updateMask.fieldPaths=lastUpdated`, updatePayload, firestoreParams);
        check(patchRes, {
            'firestore write success': (r) => r.status === 200,
        }) || errorCounter.add(1);
        firestoreWriteDuration.add(patchRes.timings.duration);

        // 3. Gemini Decision Simulation
        const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${GEMINI_KEY}`;
        const geminiPayload = JSON.stringify({
            contents: [{
                parts: [{
                    text: "Categorize these apps by network priority: YouTube, WhatsApp, Chrome. Current Latency: 45ms."
                }]
            }]
        });

        const geminiRes = http.post(geminiUrl, geminiPayload, { headers: { 'Content-Type': 'application/json' } });
        check(geminiRes, {
            'gemini api success': (r) => r.status === 200,
        }) || errorCounter.add(1);
        geminiApiDuration.add(geminiRes.timings.duration);
    }

    sleep(2); // Realistic user pacing
}
