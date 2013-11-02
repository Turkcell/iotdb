package com.turkcellteknoloji.iotdb.persistence

import java.util.UUID

/**
 * Created by capacman on 10/26/13.
 */
package object jdbc {

  private[jdbc] final class RichUUID(val self: UUID) {
    def asString = {
      self.toString.filter(_ != '-')
    }
  }

  private[jdbc] implicit def UUID2RichUUID(uuid: UUID) = {
    new RichUUID(uuid)
  }

}