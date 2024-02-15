package org.openzal.zal.gal;

import java.util.HashMap;

public class SearchGalProperty
{
  private final GalContactTags mName;
  private final String mValue;

  public SearchGalProperty(
    GalContactTags name,
    String value
  )
  {
    mName = name;
    mValue = value;
  }

  public GalContactTags  getName()
  {
    return mName;
  }

  public String getValue()
  {
    return mValue;
  }

  public enum GalContactTags
  {
    LastName,
    FirstName,
    EmailAddress,
    EmailAlias,
    DisplayName,
    Phone,
    MobilePhone,
    HomePhone,
    Title,
    Office,
    Alias,
    Company
  }

  public final static HashMap<String, GalContactTags> GAL_CONTACT_TAGS = new HashMap<>();
  public final static HashMap<String, GalContactTags> GAL_REGEX_CONTACT_TAGS = new HashMap<>();

  static {
    GAL_CONTACT_TAGS.put("lastName", GalContactTags.LastName);
    GAL_CONTACT_TAGS.put("firstName", GalContactTags.FirstName);
    GAL_CONTACT_TAGS.put("email", GalContactTags.EmailAddress);
    GAL_CONTACT_TAGS.put("fullName", GalContactTags.DisplayName);
    GAL_CONTACT_TAGS.put("workPhone", GalContactTags.Phone);
    GAL_CONTACT_TAGS.put("mobilePhone", GalContactTags.MobilePhone);
    GAL_CONTACT_TAGS.put("homePhone", GalContactTags.HomePhone);
    GAL_CONTACT_TAGS.put("jobTitle", GalContactTags.Title);
    GAL_CONTACT_TAGS.put("office", GalContactTags.Office);
    GAL_CONTACT_TAGS.put("nickname", GalContactTags.Alias);
    GAL_CONTACT_TAGS.put("company", GalContactTags.Company);

    GAL_REGEX_CONTACT_TAGS.put("email[0-9]+", GalContactTags.EmailAlias);
  }

}
