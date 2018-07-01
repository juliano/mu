/*
 * Copyright 2017-2018 47 Degrees, LLC. <http://www.47deg.com>
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

package freestyle.rpc
package server.handlers

import cats.data.Kleisli
import cats.effect.Sync
import freestyle.rpc.server.{GrpcServer, GrpcServerOps}
import io.grpc.{Server, ServerServiceDefinition}

import scala.collection.JavaConverters._
import scala.concurrent.duration.TimeUnit

private[handlers] class GrpcServerHandler[F[_]: Sync] private[GrpcServerHandler] ()
    extends GrpcServer[GrpcServerOps[F, ?]] {

  def start: GrpcServerOps[F, Server] = captureWithServer { server =>
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        server.shutdown()
        (): Unit
      }
    })

    server.start()
  }

  def getPort: GrpcServerOps[F, Int] = captureWithServer(_.getPort)

  def getServices: GrpcServerOps[F, List[ServerServiceDefinition]] =
    captureWithServer(_.getServices.asScala.toList)

  def getImmutableServices: GrpcServerOps[F, List[ServerServiceDefinition]] =
    captureWithServer(_.getImmutableServices.asScala.toList)

  def getMutableServices: GrpcServerOps[F, List[ServerServiceDefinition]] =
    captureWithServer(_.getMutableServices.asScala.toList)

  def shutdown: GrpcServerOps[F, Server] = captureWithServer(_.shutdown())

  def shutdownNow: GrpcServerOps[F, Server] = captureWithServer(_.shutdownNow())

  def isShutdown: GrpcServerOps[F, Boolean] = captureWithServer(_.isShutdown)

  def isTerminated: GrpcServerOps[F, Boolean] = captureWithServer(_.isTerminated)

  def awaitTerminationTimeout(timeout: Long, unit: TimeUnit): GrpcServerOps[F, Boolean] =
    captureWithServer(_.awaitTermination(timeout, unit))

  def awaitTermination: GrpcServerOps[F, Unit] = captureWithServer(_.awaitTermination())

  private[this] def captureWithServer[A](f: Server => A): GrpcServerOps[F, A] =
    Kleisli(s => Sync[F].delay(f(s)))

}

object GrpcServerHandler {
  def apply[F[_]](implicit F: Sync[F]): GrpcServer[GrpcServerOps[F, ?]] =
    new GrpcServerHandler[F]()(F)
}
