/* Copyright 2011 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.cloudsupport

/**
 * @author Burt Beckwith
 */
abstract class AbstractCloudTagLib {

	def pluginManager

	/**
	 * Creates a link that opens the H2 database console using connect information from the cloud environment.
	 *
	 * @attr name if specified the link will be for that service, otherwise the first JDBC service will be used
	 * @attr consolePath optional - the root of the uri to the console; usually not necessary but useful if you have a url mapping
	 */
	Closure dbconsoleLink = { attrs, body ->

		String name = attrs.name ?: null
		def connectInfo = findDbConnectInfo(name)
		if (!connectInfo) {
			if (name) {
				log.warn "No service found with name '$name'"
			}
			else {
				log.warn "No MySQL or PostgreSQL service found"
			}
			return
		}

		String dbDriver = URLEncoder.encode(connectInfo.driver, 'UTF-8')
		String dbUrl = URLEncoder.encode(connectInfo.url, 'UTF-8')
		String dbUser = URLEncoder.encode(connectInfo.userName, 'UTF-8')
		String dbPassword = URLEncoder.encode(connectInfo.password, 'UTF-8')

		String consolePath
		if (attrs.consolePath) {
			consolePath = attrs.consolePath
		}
		else {
			if (pluginManager.hasGrailsPlugin('dbconsole')) {
				// not configurable in the plugin
				consolePath = '/dbconsole'
			}
			else {
				consolePath = grailsApplication.config.grails.dbconsole.urlRoot ?: '/dbconsole'
			}
		}

		out << """<a href='javascript:void(0)' onclick='openDbConsole()'>${body()}</a>
<script>
function openDbConsole() {
 \$.get('${request.contextPath}$consolePath/login.do', function(html) {
	 var start = html.indexOf('login.jsp?jsessionid=');
	 var end = html.indexOf("'", start + 1);
	 var jsessionid = html.substring(start + 21, end);
	 location.href = '${request.contextPath}$consolePath/login.do?driver=${dbDriver}&url=${dbUrl}&user=${dbUser}&password=${dbPassword}&jsessionid=' + jsessionid;
 });
}
</script>
"""
	}

	/**
	 * Return JDBC connect info. Return an empty or null map if the specified data wasn't found
	 * or if no name is specified and no info was found at all.
	 * Values should include:
	 * 	url
	 * 	userName
	 * 	password
	 * 	driver
	 * @param name optional service name; if not specifed return the first one found
	 * @return the data
	 */
	protected abstract Map findDbConnectInfo(String name)
}
