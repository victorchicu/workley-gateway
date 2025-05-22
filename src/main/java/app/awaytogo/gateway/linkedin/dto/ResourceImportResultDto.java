package app.awaytogo.gateway.linkedin.dto;

import app.awaytogo.gateway.linkedin.types.ImType;
import app.awaytogo.gateway.linkedin.types.PhoneType;

import java.util.List;

public class ResourceImportResultDto {
    // Basic info
    private String firstName;
    private String lastName;
    private String additionalName;
    private String headline;
    // Current position
    private String position;
    private boolean showCurrentCompanyIntro;
    private String industry;
    // Education
    private String school;
    private boolean showSchoolIntro;
    // Location
    private String countryRegion;
    private String city;
    // Contact info
    private String profileUrl;
    private String email;
    private String phoneNumber;
    private PhoneType phoneType;
    private String address;
    private BirthdayDto birthdayDto;
    // Website
    private List<WebsiteDto> websites;
    // Instant Messaging
    private String username;
    private List<ImType> imTypes;
    // Premium custom button

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isShowCurrentCompanyIntro() {
        return showCurrentCompanyIntro;
    }

    public void setShowCurrentCompanyIntro(boolean showCurrentCompanyIntro) {
        this.showCurrentCompanyIntro = showCurrentCompanyIntro;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public boolean isShowSchoolIntro() {
        return showSchoolIntro;
    }

    public void setShowSchoolIntro(boolean showSchoolIntro) {
        this.showSchoolIntro = showSchoolIntro;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PhoneType getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(PhoneType phoneType) {
        this.phoneType = phoneType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BirthdayDto getBirthdayDto() {
        return birthdayDto;
    }

    public void setBirthdayDto(BirthdayDto birthdayDto) {
        this.birthdayDto = birthdayDto;
    }

    public List<WebsiteDto> getWebsites() {
        return websites;
    }

    public void setWebsites(List<WebsiteDto> websites) {
        this.websites = websites;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<ImType> getImTypes() {
        return imTypes;
    }

    public void setImTypes(List<ImType> imTypes) {
        this.imTypes = imTypes;
    }
}
