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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Gaurav Gupta
 */

@RunWith(Arquillian.class)
@RunAsClient
public class SecurityAuditTest extends ConsoleDeployment {

    @Test
    public void security_audit_contains_expected_entries() {

        JsonArray audits = getJsonArray("security/audit");

        // ---- basic expectations ----
        assertNotNull(audits);
        assertTrue(
                "Expected at least 3 security audit entries",
                audits.size() >= 3
        );

        Set<String> annotationsSeen = new HashSet<>();
        Set<String> pathsSeen = new HashSet<>();

        for (JsonValue v : audits) {
            assertEquals(JsonValue.ValueType.OBJECT, v.getValueType());

            JsonObject o = v.asJsonObject();

            // ---- schema assertions ----
            assertTrue(o.containsKey("className"));
            assertTrue(o.containsKey("methodName"));
            assertTrue(o.containsKey("httpMethods"));
            assertTrue(o.containsKey("paths"));
            assertTrue(o.containsKey("security"));

            // ---- type safety ----
            assertEquals(JsonValue.ValueType.STRING, o.get("className").getValueType());
            assertEquals(JsonValue.ValueType.STRING, o.get("methodName").getValueType());
            assertEquals(JsonValue.ValueType.ARRAY, o.get("httpMethods").getValueType());
            assertEquals(JsonValue.ValueType.ARRAY, o.get("paths").getValueType());
            assertEquals(JsonValue.ValueType.ARRAY, o.get("security").getValueType());

            // collect paths
            for (JsonValue p : o.getJsonArray("paths")) {
                pathsSeen.add(((JsonString) p).getString());
            }

            // collect security annotations
            for (JsonValue s : o.getJsonArray("security")) {
                annotationsSeen.add(((JsonString) s).getString());
            }
        }

        // ---- verify all paths ----
        assertTrue(pathsSeen.contains("denyAllPath"));
        assertTrue(pathsSeen.contains("permitAllPath"));
        assertTrue(pathsSeen.contains("userRolesAllowedPath"));

        // ---- verify all annotations ----
        assertTrue(annotationsSeen.contains("DenyAll"));
        assertTrue(annotationsSeen.contains("PermitAll"));
        assertTrue(annotationsSeen.contains("RolesAllowed"));
    }
}
