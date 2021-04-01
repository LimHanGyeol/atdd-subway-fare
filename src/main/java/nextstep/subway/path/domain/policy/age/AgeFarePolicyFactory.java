package nextstep.subway.path.domain.policy.age;

public class AgeFarePolicyFactory {

    private static final int YOUTH_AGE_MAX = 18;
    private static final int YOUTH_AGE_MIN = 13;
    private static final int CHILD_AGE_MAX = 12;
    private static final int CHILD_AGE_MIN = 6;

    public static AgeFarePolicy from(int age) {
        if (age >= YOUTH_AGE_MIN && age <= YOUTH_AGE_MAX) {
            return new AgeFareYouthPolicy();
        }
        if (age >= CHILD_AGE_MIN && age <= CHILD_AGE_MAX) {
            return new AgeFareChildPolicy();
        }
        return new AgeFareAdultPolicy();
    }
}
