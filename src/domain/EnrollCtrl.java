package domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;


public class EnrollCtrl {
    public void enroll(Student student, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Course, Double> allCourses = new HashMap<>();
        for (Map<Course, Double> coursesInTerm : student.getTranscript().values()) {
            allCourses.putAll(coursesInTerm);
        }
        for (CSE course : courses) {
            checkIfPassed(course.getCourse(), allCourses);
            checkPrerequisites(course.getCourse(), allCourses);
            checkForConflicts(course, courses);
        }
        int unitsRequested = calculateUnitsRequested(courses);
        checkUnitsRequested(unitsRequested, allCourses);
        for (CSE course : courses) {
            student.takeCourse(course.getCourse(), course.getSection());
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

    private int calculateUnitsRequested(List<CSE> courses) {
        int unitsRequested = 0;
        for (CSE course : courses) {
            unitsRequested += course.getCourse().getUnits();
        }
        return unitsRequested;
    }
    private void checkForConflicts(CSE currentCourse, List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE course : courses) {
            if (currentCourse == course) {
                continue;
            }
            if (currentCourse.getExamTime().equals(course.getExamTime())) {
                throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", currentCourse, course));
            }
            if (currentCourse.getCourse().equals(course.getCourse())) {
                throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", currentCourse.getCourse().getName()));
            }
        }
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
