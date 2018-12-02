package eShop

class Misc {

  sealed trait DeliveryMethod

  case class StringDelivery(id: String) extends DeliveryMethod

  sealed trait PaymentMethod

  case class StringPayment(id: String) extends PaymentMethod

}
