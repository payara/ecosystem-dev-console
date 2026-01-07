/*
 *
 * Copyright (c) 2026 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.console.dev.resources;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@RunAsClient
public class ConsoleRestTest extends ConsoleDeployment {

    @Test
    public void rest_resources_schema_is_valid() {
        JsonArray resources = getJsonArray("rest/resources");

        // At least the test resource must be present
        assertFalse("Expected at least one REST resource", resources.isEmpty());

        boolean foundPing = false;

        for (JsonObject r : resources.getValuesAs(JsonObject.class)) {
            assertTrue(r.containsKey("className"));
            assertTrue(r.containsKey("path"));

            if ("/__ping".equals(r.getString("path"))) {
                foundPing = true;
                assertTrue(r.getString("className").contains("TestRestResource"));
            }
        }

        assertTrue("Expected __ping test resource to be present", foundPing);
    }

    @Test
    public void rest_method_full_endpoint_returns_records_array() {
        JsonArray methods = getJsonArray("rest/methods");

        assertFalse("Expected at least one REST method", methods.isEmpty());

        JsonObject pingMethod = null;

        for (JsonObject m : methods.getValuesAs(JsonObject.class)) {
            String path = m.getString("path", null);
            if ("/__ping".equals(path)) {
                pingMethod = m;
                break;
            }
        }

        assertNotNull("Expected __ping REST method", pingMethod);
        String signature = pingMethod.getString("methodSignature");
        String encodedSignature
                = URLEncoder.encode(signature, StandardCharsets.UTF_8);
        JsonObject full = getJsonObject("rest/methods/" + encodedSignature);

        assertTrue(full.containsKey("invoked"));
    }

    @Test
    public void rest_exception_mappers_schema_is_valid() {
        JsonObject mappers = getJsonObject("rest/exception-mappers");

        // Must not be empty because a test mapper is deployed
        assertTrue("Expected at least one exception mapper",
                !mappers.isEmpty());

        // Validate schema
        for (String exceptionType : mappers.keySet()) {

            // key = exception class name
            assertTrue(exceptionType.contains("."));

            // value = mapper class name
            assertTrue(
                    mappers.get(exceptionType).getValueType()
                    == jakarta.json.JsonValue.ValueType.STRING
            );

            String mapperClass = mappers.getString(exceptionType);
            assertTrue(!mapperClass.isBlank());
        }
    }

}
