package be.planetsizebrain.crash.command.file.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.liferay.portal.model.User;

// TODO check this way of working
//@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
public interface UserMixin extends User {

	@Override
	@JsonProperty
	long getUserId();

	@Override
	@JsonProperty
	String getFullName();

	@Override
	@JsonProperty
	String getScreenName();

	@Override
	@JsonProperty
	String getEmailAddress();

//	@Override
//	@JsonIgnore
//	boolean getFemale() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	boolean getMale() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	boolean getPasswordModified();
//
//	@Override
//	@JsonIgnore
//	boolean getPasswordEncrypted();
//
//	@Override
//	@JsonIgnore
//	boolean getAgreedToTermsOfUse();
//
//	@Override
//	@JsonIgnore
//	boolean getDefaultUser();
//
//	@Override
//	@JsonIgnore
//	boolean isPasswordReset();
//
//	@Override
//	@JsonIgnore
//	boolean isLockout();
//
//	@Override
//	@JsonIgnore
//	boolean isEmailAddressVerified();
//
//	@Override
//	@JsonIgnore
//	Group getGroup() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	long getGroupId() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	int getPrivateLayoutsPageCount() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	int getPublicLayoutsPageCount() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Group> getMySiteGroups() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Group> getMySites() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Organization> getOrganizations() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	long[] getOrganizationIds() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Group> getSiteGroups() throws PortalException, SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Role> getRoles() throws SystemException;
//
//	@Override
//	@JsonIgnore
//	long[] getRoleIds() throws SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Team> getTeams() throws SystemException;
//
//	@Override
//	@JsonIgnore
//	long[] getTeamIds() throws SystemException;
//
//	@Override
//	@JsonIgnore
//	long[] getUserGroupIds() throws SystemException;
//
//	@Override
//	@JsonIgnore
//	List<UserGroup> getUserGroups() throws SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Website> getWebsites() throws SystemException;
//
//	@Override
//	@JsonIgnore
//	List<Group> getGroups() throws SystemException;
}