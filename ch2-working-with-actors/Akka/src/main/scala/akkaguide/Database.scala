package akkaguide

import scala.collection.concurrent.TrieMap

trait Database[DATA, ID] {
  def create(id: ID, data: DATA): ID
  def read(id: ID): Option[DATA]
  def update(id: ID, data: DATA)
  def delete(id: ID): Boolean
  def find(data: DATA): Option[(ID, DATA)]
}
object Database {

  def connect[DATA, ID](service: String): Database[DATA, ID] = {
    new Database[DATA, ID] {

      private val store = TrieMap[ID, DATA]()

      def create(id: ID, data: DATA): ID = {
        store += (id → data)
        id
      }

      def read(id: ID): Option[DATA] = {
        store.get(id)
      }

      def update(id: ID, data: DATA) {
        for (item <- store.get(id)) yield {
          store += (id → data)
          data
        }
      }

      def delete(id: ID): Boolean = {
        store -= id
        !store.contains(id)
      }

      def find(data: DATA): Option[(ID, DATA)] = {
        store.find(_._2 == data)
      }
    }
  }
}
