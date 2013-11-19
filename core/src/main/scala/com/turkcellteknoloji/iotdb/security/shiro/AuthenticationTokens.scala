/*
 * Copyright 2013 Turkcell Teknoloji Inc. and individual
 * contributors by the 'Created by' comments.
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

package com.turkcellteknoloji.iotdb
package security.shiro

import org.apache.shiro.authc.AuthenticationToken
import java.util.UUID
import com.turkcellteknoloji.iotdb.security.AuthPrincipalType._


case class AuthPrincipalInfo(`type`: AuthPrincipalType, uuid: UUID)

trait PrincipalAuthenticationToken extends AuthenticationToken {
  def authPrincipalType: AuthPrincipalType

  def principalID: UUID
}

object TokenType extends Enumeration {
  type TokenType = Value
  val Access, Activate, ResetPW, Confirm = Value
}

case class UsernamePasswordToken(username: String, password: String, principalType: AuthPrincipalType) extends AuthenticationToken {
  def getPrincipal = username

  def getCredentials = password
}







