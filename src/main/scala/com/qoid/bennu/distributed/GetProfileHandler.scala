package com.qoid.bennu.distributed

import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import java.sql.{ Connection => JdbcConn }

class GetProfileHandler extends DistributedRequestHandler {
  def handle(request: DistributedRequest, connection: Connection)(implicit jdbcConn: JdbcConn): DistributedResponse = {
    val alias = Alias.fetch(connection.aliasIid)

    DistributedResponse(
      request.iid,
      connection.localPeerId,
      connection.remotePeerId,
      alias.profile
    )
  }
}
