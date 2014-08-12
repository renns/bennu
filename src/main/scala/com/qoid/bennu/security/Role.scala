package com.qoid.bennu.security

import com.qoid.bennu.Enum
import com.qoid.bennu.EnumCompanion
import com.qoid.bennu.mapper.BennuMapperCompanion
import com.qoid.bennu.model._

sealed trait Role extends Enum[Role] {
  override val companion = Role

  val canExportAgent = false
  val canSpawnSession = false

  protected val permissions: Map[BennuMapperCompanion[_], List[Permission]]

  def hasPermission(permissionType: BennuMapperCompanion[_], permission: Permission): Boolean = {
    permissions.getOrElse(permissionType, Nil).contains(permission)
  }
}

object Role extends EnumCompanion[Role] {
  import com.qoid.bennu.security.Permission._

  case object AgentAdmin extends Role {
    override val canExportAgent = true

    override val permissions = Map[BennuMapperCompanion[_], List[Permission]](
      Agent -> List(View, Insert, Update, Delete),
      Alias -> List(View, Insert, Update, Delete),
      Connection -> List(View, Insert, Update, Delete),
      Content -> List(View, Insert, Update, Delete),
      Introduction -> List(View, Insert, Update, Delete),
      Label -> List(View, Insert, Update, Delete),
      LabelAcl -> List(View, Insert, Update, Delete),
      LabelChild -> List(View, Insert, Update, Delete),
      LabeledContent -> List(View, Insert, Update, Delete),
      Login -> List(View, Insert, Update, Delete),
      Notification -> List(View, Insert, Update, Delete),
      Profile -> List(View, Insert, Update, Delete)
    )
  }

  case object AliasAdmin extends Role {
    override val canSpawnSession = true

    override val permissions = Map[BennuMapperCompanion[_], List[Permission]](
      Agent -> List(View),
      Alias -> List(View, Insert, Update, Delete),
      Connection -> List(View, Insert, Update, Delete),
      Content -> List(View, Insert, Update),
      Introduction -> List(View, Insert, Update, Delete),
      Label -> List(View, Insert, Update),
      LabelAcl -> List(View, Insert, Update, Delete),
      LabelChild -> List(View, Insert, Update, Delete),
      LabeledContent -> List(View, Insert, Update, Delete),
      Login -> List(View, Insert, Update, Delete),
      Notification -> List(View, Insert, Update, Delete),
      Profile -> List(View, Insert, Update, Delete)
    )
  }

  case object ContentViewer extends Role {
    override val permissions = Map[BennuMapperCompanion[_], List[Permission]](
      Content -> List(View),
      Label -> List(View),
      LabelChild -> List(View),
      LabeledContent -> List(View)
    )
  }

  case object ProfileViewer extends Role {
    override val permissions = Map[BennuMapperCompanion[_], List[Permission]](
      Profile -> List(View)
    )
  }

  override val values: Set[Role] = Set(
    AgentAdmin,
    AliasAdmin,
    ContentViewer,
    ProfileViewer
  )
}
