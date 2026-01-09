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
import jakarta.json.JsonValue;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
@RunAsClient
public class CDInvocationTest extends ConsoleDeployment {

    @Test
    public void producers_schema_is_valid() {
        JsonArray producers = getJsonArray("cdi/producers");

        if (!producers.isEmpty()) {
            JsonObject p = producers.getJsonObject(0);

            assertTrue(p.containsKey("className"));
            assertTrue(p.containsKey("producedCount"));
            assertEquals(JsonValue.ValueType.NUMBER,
                    p.get("producedCount").getValueType());
        }
    }

    @Test
    public void interceptors_schema_is_valid() {
        JsonArray interceptors = getJsonArray("cdi/interceptors");

        if (!interceptors.isEmpty()) {
            JsonObject i = interceptors.getJsonObject(0);

            assertTrue(i.containsKey("className"));
            assertTrue(i.containsKey("invokedCount"));
        }
    }

    @Test
    public void decorators_schema_is_valid() {
        JsonArray decorators = getJsonArray("cdi/decorators");

        if (!decorators.isEmpty()) {
            JsonObject d = decorators.getJsonObject(0);

            assertTrue(d.containsKey("className"));
            assertTrue(d.containsKey("invokedCount"));
        }
    }

    @Test
    public void intercepted_classes_have_className() {
        JsonArray intercepted = getJsonArray("cdi/intercepted-classes");

        if (!intercepted.isEmpty()) {
            JsonObject obj = intercepted.getJsonObject(0);
            assertTrue(obj.containsKey("className"));
            assertTrue(obj.containsKey("interceptorBindings"));
        }
    }

    @Test
    public void decorated_classes_have_className() {
        JsonArray decorated = getJsonArray("cdi/decorated-classes");

        if (!decorated.isEmpty()) {
            JsonObject obj = decorated.getJsonObject(0);
            assertTrue(obj.containsKey("className"));
            assertTrue(obj.containsKey("decoratorBindings"));
        }
    }
}
