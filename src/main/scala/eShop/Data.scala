package eShop

import akka.actor.ActorRef

object Data {
  case class CheckoutData(
                           itemIds: List[String] = List(),
                           deliveryMethod: Option[String] = None,
                           paymentMethod: Option[String] = None
                         )
  case class CartItems(items: List[String] = List())

  case class CartInfo(cartRef: ActorRef, itemIds: List[String])

  sealed trait OrderManagerData
  object OrderManagerData {
    case class Empty() extends OrderManagerData
    case class CartData(cartRef: ActorRef) extends OrderManagerData
    case class CartDataWithSender(cartRef: ActorRef, sender: ActorRef) extends OrderManagerData
    case class InCheckoutData(checkoutRef: ActorRef) extends OrderManagerData
    case class InCheckoutDataWithSender(checkoutRef: ActorRef, sender: ActorRef) extends OrderManagerData
    case class InPaymentData(paymentRef: ActorRef) extends OrderManagerData
    case class InPaymentDataWithSender(paymentRef: ActorRef, sender: ActorRef) extends OrderManagerData
  }
}
