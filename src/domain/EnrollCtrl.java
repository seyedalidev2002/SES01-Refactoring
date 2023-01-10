package domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
    public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Course, Double> transcript = new HashMap<>() ;
        for ( Map<Course, Double> a : s.getTranscript().values()){
            transcript.putAll(a);
        }
        for (CSE o : courses) {
            checkIfPassed(o.getCourse(), transcript);
            checkPrerequisites(o.getCourse(), transcript);
            checkForConflicts(o, courses);
        }
        int unitsRequested = calculateUnitsRequested(courses);
        checkUnitsRequested(unitsRequested, transcript);
        for (CSE o : courses) {
            s.takeCourse(o.getCourse(), o.getSection());
        }
    }

    private void checkIfPassed(Course course, Map<Course, Double> transcript) throws EnrollmentRulesViolationException {
        if (transcript.containsKey(course) && transcript.get(course) >= 10) {
            throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", course.getName()));
        }
    }

    private void checkPrerequisites(Course course, Map<Course, Double> transcript) throws EnrollmentRulesViolationException {
        for (Course pre : course.getPrerequisites()) {
            if ((!transcript.containsKey(pre)) || transcript.get(pre) < 10) {
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), course.getName()));
            }
        }
    }

    private void checkForConflicts(CSE o, List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o2 : courses) {
            if (o == o2) {
                continue;
            }
            if (o.getExamTime().equals(o2.getExamTime())) {
                throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
            }
            if (o.getCourse().equals(o2.getCourse())) {
                throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
            }
        }
    }

    private int calculateUnitsRequested(List<CSE> courses) {
        int unitsRequested = 0;
        for (CSE o : courses) {
            unitsRequested += o.getCourse().getUnits();
        }
        return unitsRequested;
    }

    private void checkUnitsRequested(int unitsRequested, Map<Course, Double> transcript) throws EnrollmentRulesViolationException {
        double points = 0;
        int totalUnits = 0;
        for (Map.Entry<Course, Double> record : transcript.entrySet()) {
            points += record.getValue() * record.getKey().getUnits();
            totalUnits += record.getKey().getUnits();
        }
        double gpa = points / totalUnits;
        if ((gpa < 12 && unitsRequested > 14) ||
                (gpa < 16 && unitsRequested > 16) ||
                (unitsRequested > 20)) {
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, gpa));
        }
    }
}
