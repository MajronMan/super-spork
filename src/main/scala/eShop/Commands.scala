package eShop

import akka.actor.ActorRef
import eShop.Data.CartInfo

object Commands {

  sealed trait Command

  object Checkout {

    case class StartCheckout(cartInfo: CartInfo) extends Command

    case class CancelCheckout() extends Command

    case class SelectPaymentMethod(method: String) extends Command

    case class SelectDeliveryMethod(method: String) extends Command

    case class ExpireCheckoutTimer() extends Command

    case class ExpirePaymentTimer() extends Command

    case class ReceivePayment() extends Command
  }

  object Cart {

    case class AddItem(id: String) extends Command

    case class RemoveItem(id: String) extends Command

    case class StartCheckout(checkoutRef: ActorRef) extends Command

    case class CancelCheckout() extends Command

    case class CloseCheckout() extends Command

    case class ExpireTimer() extends Command

  }

  object OrderManager {
    case class AddItem(id: String) extends Command
    case class RemoveItem(id: String) extends Command
    case class Buy() extends Command
    case class SelectDeliveryAndPaymentMethod(delivery: String, payment: String) extends Command
    case class Pay() extends Command
  }

  sealed trait Ack
  case object Done extends Ack

}

