
package com.rootcauseai.rootcause_ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_name")
    private String companyName;
    @Column(name = "job_role")
    private String jobRole;
    @Column(name = "recruiter_email")
    private String recruiterEmail;
    @Column(name = "website_url")
    private String websiteUrl;

    public Company() {
    }

    public Long getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public String getRecruiterEmail() {
        return recruiterEmail;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public void setRecruiterEmail(String recruiterEmail) {
        this.recruiterEmail = recruiterEmail;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}
