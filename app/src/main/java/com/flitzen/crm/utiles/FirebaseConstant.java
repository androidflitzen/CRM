package com.flitzen.crm.utiles;

public class FirebaseConstant {

    public static String companyLogoFolder="/Company Logo/";

    public static class User{
        public static String User_TABLE="Users";

        public static String id="id";
        public static String user_name="userName";
        public static String user_email="userEmail";
        public static String user_password="userPassword";
    }


    public static class Currency{
        public static String Currency_TABLE="Currency";

        public static String id="id";
        public static String currencyName="currencyName";

    }

    public static class Company{
        public static String Company_TABLE="Company";

        public static String id="id";
        public static String companyName="companyName";
        public static String companyLogo="companyLogo";
        public static String baseCurrency="baseCurrency";
        public static String userId="userId";
        public static String companyType="companyType";                   // 0-Product , 1-Service  company type

    }

    public static class EnquirySource{
        public static String EnquirySource_TABLE="Enquiry Source";

        public static String id="id";
        public static String enquirySource="enquirySource";

    }

    public static class Enquiry{
        public static String Enquiry_TABLE="Enquiry";

        public static String id="id";
        public static String userId="userId";
        public static String companyName="companyName";
        public static String personName="personName";
        public static String email="email";                             // Not required field
        public static String contactNo="contactNo";
        public static String enquiryFor="enquiryFor";
        public static String enquirySource="enquirySource";
        public static String importance="importance";
        public static String otherDetails="otherDetails";              // Not required field
        public static String enquiryDate="enquiryDate";
        public static String enquiryTime="enquiryTime";

    }

}
