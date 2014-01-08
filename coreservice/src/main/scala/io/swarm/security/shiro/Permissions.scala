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

import io.swarm.domain
import org.apache.shiro.authz.Permission

/**
 * Created by Anil Chalil on 11/19/13.
 */
object Permissions {
  val allRights = "admin,access,get,put,post,delete".split(",").toSet

  def apply(permission: String) = new CustomPermission(permission)

  def apply(permissions: Set[String]): Set[Permission] = permissions.map(new CustomPermission(_))

  def apply(orgRefs: domain.OrganizationRef*) = {
    new CustomPermission(s"organizations:admin,access,get,put,post,delete:${orgRefs.map(_.id).mkString(",")}")
  }

  def apply[O <: domain.OrganizationRef](orgRefs: Set[O]) = {
    new CustomPermission(s"organizations:admin,access,get,put,post,delete:${orgRefs.map(_.id).mkString(",")}")
  }

  def forDatabases(dbRefs: domain.DatabaseRef*) = {
    new CustomPermission(s"databases:admin,access,get,put,post,delete:${dbRefs.map(_.id).mkString(",")}")
  }

  def forDatabases[D <: domain.DatabaseRef](dbRefs: Set[D]) = {
    new CustomPermission(s"databases:admin,access,get,put,post,delete:${dbRefs.map(_.id).mkString(",")}")
  }

  def isValidRights(vals: Iterable[String]) = vals.forall(allRights.contains(_))
}