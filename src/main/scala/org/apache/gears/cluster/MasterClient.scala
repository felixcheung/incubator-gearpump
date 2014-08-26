/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.gears.cluster

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import org.apache.gears.cluster.ClientToMaster._
import org.apache.gears.cluster.MasterToClient.{ShutdownApplicationResult, SubmitApplicationResult}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class MasterClient(master : ActorRef) {
  private implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  /**
   * return appId
   * throw if anything wrong
   */
  def submitApplication(appMaster : Class[_ <: Actor], config : Configs, app : Application) : Int = {
    val result = Await.result( (master ? SubmitApplication(appMaster, config, app)).asInstanceOf[Future[SubmitApplicationResult]], Duration.Inf)
    result.appId match {
      case Success(appId) => appId
      case Failure(ex) => throw(ex)
    }
  }

  /**
   * Throw exception if fail to shutdown
   */
  def shutdownApplication(appId : Int) : Unit = {
    val result = Await.result((master ? ShutdownApplication(appId)).asInstanceOf[Future[ShutdownApplicationResult]], Duration.Inf)
    result.appId match {
      case Success(_) => Unit
      case Failure(ex) => throw(ex)
    }
  }
}