import filters.NoCache
import play.api.mvc.WithFilters

object Global extends WithFilters(NoCache)
