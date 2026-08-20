
package com.tremtechzim.pos
import java.security.MessageDigest
import java.security.SecureRandom

object Security {
 fun hash(password:String,salt:ByteArray):String{
  val md=MessageDigest.getInstance("SHA-256")
  return (salt + md.digest(salt + password.toByteArray())).joinToString(""){"%02x".format(it)}
 }
 fun newSalt():ByteArray=ByteArray(16).also{SecureRandom().nextBytes(it)}
}