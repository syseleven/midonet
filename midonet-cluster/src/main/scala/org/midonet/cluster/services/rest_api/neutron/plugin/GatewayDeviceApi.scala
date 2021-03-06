/*
 * Copyright 2015 Midokura SARL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.midonet.cluster.services.rest_api.neutron.plugin

import java.util
import java.util.UUID

import org.midonet.cluster.rest_api.neutron.models.{RemoteMacEntry, GatewayDevice}
import org.midonet.cluster.rest_api.{NotFoundHttpException, ConflictHttpException}

trait GatewayDeviceApi {

    @throws(classOf[NotFoundHttpException])
    def getGatewayDevice(id: UUID): GatewayDevice

    def getGatewayDevices: util.List[GatewayDevice]

    @throws(classOf[ConflictHttpException])
    @throws(classOf[NotFoundHttpException])
    def createGatewayDevice(gatewayDevice: GatewayDevice): Unit

    @throws(classOf[ConflictHttpException])
    @throws(classOf[NotFoundHttpException])
    def updateGatewayDevice(gatewayDevice: GatewayDevice): Unit

    def deleteGatewayDevice(id: UUID): Unit

    @throws(classOf[ConflictHttpException])
    @throws(classOf[NotFoundHttpException])
    def createRemoteMacEntry(entry: RemoteMacEntry): Unit

    def deleteRemoteMacEntry(id: UUID): Unit

    @throws(classOf[NotFoundHttpException])
    def getRemoteMacEntry(id: UUID): RemoteMacEntry

    def getRemoteMacEntries: util.List[RemoteMacEntry]
}
