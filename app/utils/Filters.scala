package utils

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject() (csrfFilter: CSRFFilter, corsFilter: CORSFilter, securityHeadersFilter: SecurityHeadersFilter) extends HttpFilters {
  def filters = Seq(csrfFilter, corsFilter, securityHeadersFilter)
}
