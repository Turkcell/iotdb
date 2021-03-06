/*
 * Copyright 2013 Turkcell Teknoloji Inc. and individual
 * contributors by the 'Created by' comments.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.swarm.security.shiro

import org.scalatest.FlatSpec
import org.apache.shiro.SecurityUtils
import org.scalatest.ShouldMatchers
import io.swarm.domain.UserInfo
import io.swarm.security.AuthPrincipalType.AuthPrincipalType
import org.apache.shiro.authc.AuthenticationException
import io.swarm.security.ExpiredTokenException
import io.swarm.domain.Client
import org.apache.shiro.authc.AuthenticationToken
import io.swarm.domain.IDEntity


trait BasicRealmBehaviors {
  this: FlatSpec with ShouldMatchers with InMemoryComponents =>

  def basic(validAuthToken: AuthenticationToken, invalidAuthToken: AuthenticationToken, nonExistenceAuthToken: AuthenticationToken, validToken: OauthBearerToken, expiredToken: OauthBearerToken) {
    it should "authenticate with valid token" in {
      SecurityUtils.getSubject().isAuthenticated() should be(false)
      SecurityUtils.getSubject.login(validAuthToken)
      SecurityUtils.getSubject().getPrincipal() match {
        case e: IDEntity if validAuthToken.getPrincipal().isInstanceOf[ClientID] => e.id should be(validAuthToken.getPrincipal().asInstanceOf[ClientID].principalID)
        case u: UserInfo => List(u.username, u.email) should contain(validAuthToken.getPrincipal())
        case _ => SecurityUtils.getSubject().getPrincipal() should be(validAuthToken.getPrincipal())
      }
    }

    it should "throw authentication exception with wrong credentials" in {
      intercept[AuthenticationException] {
        SecurityUtils.getSubject().isAuthenticated() should be(false)
        SecurityUtils.getSubject.login(invalidAuthToken)
      }
    }

    it should "throw authentication exception with non existent principal" in {
      intercept[AuthenticationException] {
        SecurityUtils.getSubject().isAuthenticated() should be(false)
        SecurityUtils.getSubject.login(nonExistenceAuthToken)
      }
    }
    it should "authenticate with bearer token" in {
      SecurityUtils.getSubject().isAuthenticated() should be(false)
      SecurityUtils.getSubject().login(OauthBearerToken(validToken.token))
      SecurityUtils.getSubject().getPrincipal().asInstanceOf[IDEntity].id shouldBe validToken.principalID
    }
    it should "fail with expired bearer token" in {
      intercept[ExpiredTokenException] {
        Thread.sleep(110)
        SecurityUtils.getSubject().isAuthenticated() should be(false)
        SecurityUtils.getSubject().login(OauthBearerToken(expiredToken.token))
      }
    }
  }
}

trait ClientRealmBehaviors extends BasicRealmBehaviors {
  this: FlatSpec with ShouldMatchers with InMemoryComponents =>

  def disable

  def passivate

  def revert(user: Client)

  def client(client: Client, validToken: OauthBearerToken) {
    def withDisabledUser(testCode: => Any) {
      try {
        disable
        testCode
      } finally revert(client)
    }

    def withNotActivatedUser(testCode: => Any) {
      try {
        passivate
        testCode
      } finally revert(client)
    }

    it should "fail with disabled client" in withDisabledUser {
      intercept[AuthenticationException] {
        SecurityUtils.getSubject().isAuthenticated() should be(false)
        SecurityUtils.getSubject().login(OauthBearerToken(validToken.token))
      }
    }

    it should "fail with not active client" in withNotActivatedUser {
      intercept[AuthenticationException] {
        SecurityUtils.getSubject().isAuthenticated() should be(false)
        SecurityUtils.getSubject().login(OauthBearerToken(validToken.token))
      }
    }
  }
}

trait UserRealmBehaviors extends ClientRealmBehaviors {
  this: FlatSpec with ShouldMatchers with InMemoryComponents =>
  def user(user: UserInfo, userPass: String, principalType: AuthPrincipalType) {
    it should "authenticate with email and password" in {
      SecurityUtils.getSubject().isAuthenticated() should be(false)
      SecurityUtils.getSubject.login(new UsernamePasswordToken(user.email, userPass, principalType))
      SecurityUtils.getSubject().getPrincipal().asInstanceOf[UserInfo].email should be(user.email)
    }
  }
}
