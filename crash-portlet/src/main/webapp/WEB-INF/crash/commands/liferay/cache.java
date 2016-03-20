package liferay;

import java.util.Map;

import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.liferay.portal.cache.ehcache.EhcachePortalCacheManager;
import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class cache extends BaseCommand {

	@Usage("cache info")
	@Command
	public void main(InvocationContext<Map> context) throws Exception {
		EhcachePortalCacheManager cacheManager = (EhcachePortalCacheManager) PortalBeanLocatorUtil.locate("com.liferay.portal.kernel.cache.MultiVMPortalCacheManager");

		CacheManager ehcacheManager = cacheManager.getEhcacheManager();
		for (String cacheName : ehcacheManager.getCacheNames()) {
			Cache cache = ehcacheManager.getCache(cacheName);
			Map<String, String> element = Maps.newHashMap();

			element.put("name", cacheName);
			element.put("hitCount", Long.toString(cache.getStatistics().cacheHitCount()));

			context.provide(element);
		}
	}
}