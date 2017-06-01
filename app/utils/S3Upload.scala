package utils

import awscala._, s3._

/**
  * Created by jaideep on 01/06/17.
  */


case object S3Bucket {

  implicit val s3 = S3()

  s3.at(Region0.US_EAST_1)
  def getBucketByName(name: String): Option[Bucket] = s3.bucket(name)

  def createBucket(name: String): Bucket = s3.createBucket(name)
  def createObject(bucket: Bucket, name: String, file: File): PutObjectResult = bucket.put(name, file)

}