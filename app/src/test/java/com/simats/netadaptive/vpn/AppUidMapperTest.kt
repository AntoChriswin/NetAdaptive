package com.simats.netadaptive.vpn

import android.content.pm.PackageManager
import com.simats.netadaptive.ml.AppTier
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class AppUidMapperTest {
    private val pm: PackageManager = mock {
        on { getPackagesForUid(1001) } doReturn arrayOf("com.test.app")
    }
    private val mapper = AppUidMapper(pm)

    @Test
    fun testCaching() {
        assertEquals("com.test.app", mapper.resolvePackage(1001))
        assertEquals("com.test.app", mapper.resolvePackage(1001))
        
        // PackageManager should only be called once due to cache
        verify(pm, times(1)).getPackagesForUid(1001)
    }

    @Test
    fun testTierMapping() {
        mapper.updateTierMapping(mapOf("com.test.app" to AppTier.CRITICAL))
        assertEquals(AppTier.CRITICAL, mapper.getTier("com.test.app"))
        assertEquals(AppTier.NORMAL, mapper.getTier("unknown.app"))
    }
}
