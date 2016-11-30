package com.yichao.alexa.model;

public class ReviewSummary {

    private String rating; // div.editorsRating > span.rating
    private String theGood; // div.quickInfo > .theGood > span
    private String theBad; // div.quickInfo > .theBad > span
    private String theBottomLine; // div.quickInfo > .theBottomLine > span

    public ReviewSummary() {
    }

    public ReviewSummary(String rating, String theGood, String theBad, String theBottomLine) {
        this.rating = rating;
        this.theGood = theGood;
        this.theBad = theBad;
        this.theBottomLine = theBottomLine;
    }

    public String toResponseString() {
        final String highlightBreak = "<break time=\"0.5s\"/>";
        final StringBuilder sb = new StringBuilder();
        sb.append("<s>");
        sb.append("Expert rating is ");
        sb.append(rating);
        sb.append(" out of five.");
        sb.append("</s>");
        sb.append("<s>");
        sb.append("The Good ");
        sb.append(highlightBreak);
        sb.append(theGood);
        sb.append("</s>");
        sb.append("<s>");
        sb.append("The Bad");
        sb.append(highlightBreak);
        sb.append(theBad);
        sb.append("</s>");
        sb.append("<s>");
        sb.append("The Bottom Line");
        sb.append(highlightBreak);
        sb.append(theBottomLine);
        sb.append("</s>");
        return sb.toString();
    }

    public String getRating() {
        return rating;
    }

    public String getTheGood() {
        return theGood;
    }

    public String getTheBad() {
        return theBad;
    }

    public String getTheBottomLine() {
        return theBottomLine;
    }
}
