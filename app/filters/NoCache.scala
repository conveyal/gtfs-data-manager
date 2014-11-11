// add a no-cache header to every request so IE won't break
// http://stackoverflow.com/questions/19307850

package filters

import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object NoCache extends Filter {
  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
        val result = f(rh)
        result.map(_.withHeaders("Cache-Control" -> "no-cache"))
      }
}
