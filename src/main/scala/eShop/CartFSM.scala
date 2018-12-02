package eShop

import akka.actor.{LoggingFSM, Timers}
import java.time.Duration

import eShop.Commands.Cart._
import eShop.Events.Cart._
import eShop.Events.Other._
import eShop.States.CartState
import eShop.Data.{CartInfo, CartItems}
import eShop.Commands.Checkout.{StartCheckout => CheckoutStartCheckout}

object CartFSM {
  val cartTimerId = "CartTimerId"
}

final class CartFSM(cartExpirationTime: Duration = Duration.ofSeconds(1)) extends LoggingFSM[CartState, CartItems] with Timers {
  startWith(CartState.Empty, CartItems())

  when(CartState.Empty) {
    case Event(AddItem(item), _) =>
      println("Cart: added " + item)
      goto(CartState.NonEmpty) using CartItems(List(item)) replying ItemAdded(item)
  }

  when(CartState.NonEmpty) {
    case Event(AddItem(item), CartItems(items)) =>
      println("Cart: Added " + item + "; current items: " + (item :: items))
      stay using CartItems(item :: items) replying ItemAdded(item)
    case Event(RemoveItem(item), CartItems(items)) =>
      print("Cart: ")
      if(items.contains(item)){
        print("Removed " + item + "; ")
        sender ! ItemRemoved(item)
      }

      items.diff(List(item)) match {
        case List() =>
          println("no items")
          goto(CartState.Empty) using CartItems()
        case newItems: List[String] =>
          println("current items: " + newItems)
          stay using CartItems(newItems)
      }
    case Event(TimerExpired(), CartItems(_)) =>
      println("Cart: Cart expired")
      goto(CartState.Empty) using CartItems() replying TimerExpired()
    case Event(StartCheckout(checkoutRef), cartContents) =>
      println("Cart: Starting checkout with items: " + cartContents.items)
      checkoutRef ! CheckoutStartCheckout(CartInfo(self, cartContents.items))
      goto(CartState.InCheckout) using cartContents replying CheckoutStarted()
  }

  when(CartState.InCheckout) {
    case Event(CancelCheckout(), prevData) =>
      println("Cart: Checkout cancelled")
      goto(CartState.NonEmpty) using prevData replying CheckoutCancelled()
    case Event(CloseCheckout(), _) =>
      println("Cart: Checkout closed")
      goto(CartState.Empty) using CartItems()
  }

  onTransition {
    case CartState.Empty -> CartState.NonEmpty =>
      timers.startSingleTimer(CartFSM.cartTimerId, TimerExpired(), cartExpirationTime)
    case CartState.NonEmpty -> _ =>
      timers.cancel(CartFSM.cartTimerId)
    case CartState.InCheckout -> CartState.NonEmpty =>
      timers.startSingleTimer(CartFSM.cartTimerId, TimerExpired(), cartExpirationTime)
  }

  whenUnhandled {
    case Event(TimerExpired(), _) => {
      println("Cart: timer expired")
      goto(CartState.Empty) using CartItems()
    }
    case unhandled =>
      println("Cart: Cannot handle " + unhandled)
      sender ! Failed
      stay
  }

  initialize()
}
