package app.awaytogo.gateway.resume.common.dto;

import app.awaytogo.gateway.resume.importers.linkedin.types.ImType;
import app.awaytogo.gateway.resume.importers.linkedin.types.PhoneType;

import java.util.List;

public class ResumeImportResultDto {
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
    private String country;
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

    public ResumeImportResultDto setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public ResumeImportResultDto setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public ResumeImportResultDto setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
        return this;
    }

    public String getHeadline() {
        return headline;
    }

    public ResumeImportResultDto setHeadline(String headline) {
        this.headline = headline;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public ResumeImportResultDto setPosition(String position) {
        this.position = position;
        return this;
    }

    public boolean isShowCurrentCompanyIntro() {
        return showCurrentCompanyIntro;
    }

    public ResumeImportResultDto setShowCurrentCompanyIntro(boolean showCurrentCompanyIntro) {
        this.showCurrentCompanyIntro = showCurrentCompanyIntro;
        return this;
    }

    public String getIndustry() {
        return industry;
    }

    public ResumeImportResultDto setIndustry(String industry) {
        this.industry = industry;
        return this;
    }

    public String getSchool() {
        return school;
    }

    public ResumeImportResultDto setSchool(String school) {
        this.school = school;
        return this;
    }

    public boolean isShowSchoolIntro() {
        return showSchoolIntro;
    }

    public ResumeImportResultDto setShowSchoolIntro(boolean showSchoolIntro) {
        this.showSchoolIntro = showSchoolIntro;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public ResumeImportResultDto setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getCity() {
        return city;
    }

    public ResumeImportResultDto setCity(String city) {
        this.city = city;
        return this;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public ResumeImportResultDto setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ResumeImportResultDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public ResumeImportResultDto setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public PhoneType getPhoneType() {
        return phoneType;
    }

    public ResumeImportResultDto setPhoneType(PhoneType phoneType) {
        this.phoneType = phoneType;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ResumeImportResultDto setAddress(String address) {
        this.address = address;
        return this;
    }

    public BirthdayDto getBirthdayDto() {
        return birthdayDto;
    }

    public ResumeImportResultDto setBirthdayDto(BirthdayDto birthdayDto) {
        this.birthdayDto = birthdayDto;
        return this;
    }

    public List<WebsiteDto> getWebsites() {
        return websites;
    }

    public ResumeImportResultDto setWebsites(List<WebsiteDto> websites) {
        this.websites = websites;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ResumeImportResultDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public List<ImType> getImTypes() {
        return imTypes;
    }

    public ResumeImportResultDto setImTypes(List<ImType> imTypes) {
        this.imTypes = imTypes;
        return this;
    }
}
