package storage;

import storage.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for calculating and representing the scores received by students on a test.
 */
public class StudentScores {

    private List<String> student_identifications;
    private List<Double> student_point_scores;
    private List<Double> student_percentage_scores;
    private List<String> student_letter_grades;
    private Test test;
    private double test_possible_points;

    /**
     * Converts a percentage score (generally 0 to 100, but could be outside that range in some cases) into a letter grade using the standard grading scale.
     * @param percentage_score A percentage score (usually 0 to 100, but could be outside that range in some cases).
     * @return A string with a letter grade like "A+". Scores less than zero are treated as F-'s.
     */
    private String percentageToLetterGrade(double percentage_score) {
        if (percentage_score >= 97.0) {
            return "A+";
        }
        else if (percentage_score >= 93.0) {
            return "A";
        }
        else if (percentage_score >= 90.0) {
            return "A-";
        }
        else if (percentage_score >= 87.0) {
            return "B+";
        }
        else if (percentage_score >= 83.0) {
            return "B";
        }
        else if (percentage_score >= 80.0) {
            return "B-";
        }
        else if (percentage_score >= 77.0) {
            return "C+";
        }
        else if (percentage_score >= 73.0) {
            return "C";
        }
        else if (percentage_score >= 70.0) {
            return "C-";
        }
        else if (percentage_score >= 67.0) {
            return "D+";
        }
        else if (percentage_score >= 63.0) {
            return "D";
        }
        else if (percentage_score >= 60.0) {
            return "D-";
        }
        else if (percentage_score >= 0.0) {
            return "F";
        }
        // A grade less than zero is an F-.
        return "F-";
    }

    /**
     * Calculate student scores, given a graded test.
     * @param persistentStorage A connection to the database.
     * @param user_id The user id of the owner of the test and student answers.
     * @param test_id The id of the test for which scores will be calculated. The test must have been graded already.
     */
    public StudentScores(PersistentStorage persistentStorage, int user_id, int test_id) {
        // Retrieve the requested test.
        test = persistentStorage.getTestById(user_id, test_id);
        if (test == null) {
            return;
        }
        // Calculate the total number of possible points on the test. Note that this is not the maximum number of points a student could receive because it does not include extra credit.
        TestQuestion[] testQuestions = persistentStorage.getQuestionsByTestId(user_id, test_id);
        test_possible_points = 0.0;
        for (TestQuestion testQuestion : testQuestions) {
            try {
                if (!testQuestion.getExtraCredit()) {
                    test_possible_points += Double.parseDouble(testQuestion.getPoints());
                }
            }
            catch (NumberFormatException e) {
                // Pass.
            }
        }
        // Find all students who have GRADED answers to this test.
        StudentAnswer[] students = persistentStorage.findAllStudentsWhoTookTest(user_id, test_id);
        // Retrieve the student answer sets of the students who took the test.
        List<StudentAnswerSet> studentAnswerSets = new ArrayList<>();
        for (StudentAnswer student : students) {
            studentAnswerSets.add(persistentStorage.findStudentAnswerSetWithStudentAnswer(user_id, student.getStudentAnswerId()));
        }
        // Calculate a score for each student answer set.
        student_identifications = new ArrayList<>();
        student_point_scores = new ArrayList<>();
        student_percentage_scores = new ArrayList<>();
        student_letter_grades = new ArrayList<>();
        for (StudentAnswerSet studentAnswerSet : studentAnswerSets) {
            // Get the IDs of all student answers in this set.
            Integer[] student_answer_ids = studentAnswerSet.getStudentAnswerIds();
            // Retrieve all student answers in this set.
            List<StudentAnswer> studentAnswers = new ArrayList<>();
            for (Integer student_answer_id : student_answer_ids) {
                studentAnswers.add(persistentStorage.getStudentAnswerById(user_id, student_answer_id));
            }
            // If this student answer set is empty for some reason, skip it.
            if (studentAnswers.size() == 0) {
                continue;
            }
            // Get the name of the student who wrote this answer set.
            String student_identification = studentAnswers.get(0).getStudentIdentification();
            // If the student has not been identified, skip them.
            if (student_identification.equals("")) {
                continue;
            }
            // Save the student's identification (if it exists).
            student_identifications.add(student_identification);
            // Calculate the student's point score.
            double student_point_score = 0.0;
            for (StudentAnswer studentAnswer : studentAnswers) {
                try {
                    student_point_score += Double.parseDouble(studentAnswer.getScore());
                }
                catch (NumberFormatException e) {
                    // Pass.
                }
            }
            // Save the student's point score.
            student_point_scores.add(student_point_score);
            // Calculate and save the student's percentage score.
            double student_percentage_score = (student_point_score / test_possible_points) * 100.0;
            // A test worth zero points is a special case. Automatically give 100%.
            if (test_possible_points == 0.0) {
                student_percentage_score = 100.0;
            }
            student_percentage_scores.add(student_percentage_score);
            // Save the student's letter grade.
            student_letter_grades.add(percentageToLetterGrade(student_percentage_score));
        }
    }

    /**
     * Get a list of the identifications (names or ID numbers) of all students who took the test.
     * @return A list of student identifications in an order that matches up with the scores/grades returned by the other methods of this class.
     */
    public List<String> getStudentIdentifications() {
        return student_identifications;
    }

    /**
     * Get a list of student point scores (total number of points earned on a test).
     * @return A list of student point scores in an order that matches up with the scores/grades/names returned by the other methods of this class.
     */
    public List<Double> getStudentPointScores() {
        return student_point_scores;
    }

    /**
     * Get a list of student percentage scores (total number of points earned on a test divided by possible points, multiplied by 100).
     * @return A list of student percentage scores in an order that matches up with the scores/grades/names returned by the other methods of this class.
     */
    public List<Double> getStudentPercentageScores() {
        return student_percentage_scores;
    }

    /**
     * Get a list of student letter grades (strings like "A+" or "B-" determined by the standard grading scale).
     * @return A list of student letter grades in an order that matches up with the scores/grades/names returned by the other methods of this class.
     */
    public List<String> getStudentLetterGrades() {
        return student_letter_grades;
    }

    /**
     * Get the test that these scores correspond to.
     * @return The test with the id specified when this StudentScores object was constructed.
     */
    public Test getTest() {
        return test;
    }

    /**
     * Get the number of points possible on the test. Note that this is not the maximum number of points attainable due to extra credit.
     * @return The number of points possible on the test specified when this object was constructed.
     */
    public double getTestPossiblePoints() {
        return test_possible_points;
    }
}
