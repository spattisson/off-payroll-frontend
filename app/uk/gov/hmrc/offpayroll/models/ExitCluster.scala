/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.offpayroll.models

/**
  * Created by peter on 08/01/2017.
  */
object ExitCluster extends Cluster {

  private val officeholder = "officeHolder"

  /**
    * Use this value to informatively name the cluster and use as a key to tags
    */
  override def name: String = "exit"

  /**
    * All the Elements that make up this cluster
    *
    * @return
    */
  override def clusterElements: List[Element] =
    List(
      Element(officeholder, RADIO, 0, this)
    )

  /**
    * Helps order a Cluster in an Interview
    *
    * @return
    */
  override def clusterID: Int = 1


}
