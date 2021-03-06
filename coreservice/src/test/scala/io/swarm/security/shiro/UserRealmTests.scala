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

package io.swarm.security.shiro

import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.realm.Realm
import org.scalatest.FlatSpec
import org.scalatest.ShouldMatchers
import scala.collection.JavaConverters._
import org.apache.shiro.SecurityUtils
import io.swarm.security.{TokenInfo, HashedAlgorithm, AuthPrincipalType, TokenCategory}
import io.swarm.UUIDGenerator
import io.swarm.domain.Client
import com.github.nscala_time.time.Imports._
import io.swarm.management.dao.{UserRepositoryDaoComponent, OrganizationRepositoryDaoComponent}
import io.swarm.management.Management.{UserRef, ServiceProviderRegistryComponent}

/**
 * Created by Anil Chalil on 11/15/13.
 */

class UserRealmTests extends FlatSpec with ShouldMatchers with UserRealmComponent with InMemoryComponents with RealmTestsBase with UserRealmBehaviors with HSQLInMemoryManagementDaoComponent with OrganizationRepositoryDaoComponent with UserRepositoryDaoComponent with ServiceProviderRegistryComponent {
  val realm = UserRealm
  val sec = new DefaultSecurityManager()
  sec.setAuthenticator(new ExclusiveRealmAuthenticator)
  sec.setRealms(List(realm.asInstanceOf[Realm]).asJava)
  SecurityUtils.setSecurityManager(sec)
  val user = UserRef(UUIDGenerator.randomGenerator.generate(), Some("test"), Some("test"), "test", "test@test.com", HashedAlgorithm.toHex("test"), true, true, false)
  val userPass = "test"
  db.withSession {
    implicit s =>
      managementDao.create
      userRepository.saveUserRef(user)
  }
  val validToken = {
    val tokenInfo = TokenInfo(TokenCategory.Access, TokenType.Access, AuthPrincipalInfo(AuthPrincipalType.User, user.id), 0.toDuration, 0)
    tokenRepository.putTokenInfo(tokenInfo)
    OauthBearerToken(tokenInfo)
  }
  val expiredToken = {
    val tokenInfo = TokenInfo(TokenCategory.Access, TokenType.Access, AuthPrincipalInfo(AuthPrincipalType.User, user.id), 100.toDuration, 0)
    tokenRepository.putTokenInfo(tokenInfo)
    OauthBearerToken(tokenInfo)
  }

  def disable {
    db.withSession(implicit s => userRepository.updateUserRef(user.copy(disabled = true)))
  }

  def passivate {
    db.withSession(implicit s => userRepository.updateUserRef(user.copy(activated = false)))
  }

  def revert(user: Client) {
    db.withSession(implicit s => userRepository.updateUserRef(user.asInstanceOf[UserRef]))
  }

  "DatabaseUser" should behave like basic(new UsernamePasswordToken(user.username, "test", AuthPrincipalType.User), new UsernamePasswordToken(user.username, "wrong", AuthPrincipalType.User), new UsernamePasswordToken("wrıng", "wrong", AuthPrincipalType.User), validToken, expiredToken)
  "DatabaseUser" should behave like client(user, validToken)
  "DatabaseUser" should behave like user(user, userPass, AuthPrincipalType.User)

}
