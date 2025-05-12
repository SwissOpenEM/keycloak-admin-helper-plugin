# Internals of keycloak-admin-helper-plugin

## Intro
**The plugin listens on CREATE events of type GROUP.** It doesn't take action in any other case, like UPDATE group or
deleting, not does it react on any user modification events or other types.

The plugin contains two separate event listeners, which implement different workflows. So, there are two workflows in
total.

## Workflow "SuperAdminHelper"
Target user: superuser (someone from PSI)

Trigger: CREATE GROUP event of a **top-level**-group with the suffix `--initnewfacility`.

As top-level groups can only be created by the super user, there are not more security considerations about this.

The workflow, in details:
* Sanity check. Abort in case of an error:
  * must be a top-level group
  * in front of the prefix, there must be a facility name of 1 character length minimum, It will be used in forther steps
  * collision detection. as the group will be renamed to the facility name without prefix, it is not allowed that another group already exists with this name
* Rename the group to facility name (without suffix)
* Create admin user
  * name: `facilityName + "-admin"`
  * attribute `facility-name` set to facility name
  * with `(realm-management) view-users` role
  * user is enabled, but without credentials set, so login is not yet possible
* set group attribute `facility-name` to facility name
* set permission and policy for group (exactly as below in next workflow)

## Workflow "FacilityManagerHelper"
Target users: facility managers

Trigger: CREATE GROUP event of a **sub**-group.

The workflow, in details:
* Sanity check. Abort in case of an error:
  * must be a sub group (must have a parent group)
  * extract attribute `facilityName` of top-level group. Fail if attribute is not set
* set group attribute `facility-name` to facility name
* set permission and policy for group
  * policy details. The policy will only be created if it doesn't yet exist with this name:

    | Attribute         | Value                                                                        |
    |-------------------|------------------------------------------------------------------------------|
    | Name:             | "allow " + facilityName + " admin users policy"                              |
    | Description:      | facilityName + " groups administration for " + facilityName + " admin users" |
    | DecisionStrategy: | DecisionStrategy.UNANIMOUS                                                   |
    | Logic:            | Logic.POSITIVE                                                               |
    | User:             | FacilityName + "-admin"                                                      |

  * permission details. The permission will only be created if it doesn't yet exist with this name. Otherwise, the group is added to the existing permission:

    | Attribute     | Value                                                                                                   |
    |---------------|---------------------------------------------------------------------------------------------------------|
    | Name:         | facilityName + " admin for all " + facilityName + " groups"                                             |
    | Description:  | "Allow " + facilityName + " admins to change group members and settings of " + facilityName + " groups" |
    | ResourceType: | Groups                                                                                                  |
    | Resources:    | list of groups                                                                                          |
    | Policy:       | link to policy, see above                                                                               |
    | Scopes:       | "view-members", "manage-membership", "manage-members", "view", "manage"                                 |
