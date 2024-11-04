package lotto;

import java.util.*;
import java.util.stream.Collectors;

public class Application {
    private static final int LOTTO_PRICE = 1000;
    private static final int BONUS_MATCH_RANK = 5;

    private static final Map<Integer, Integer> PRIZES = Map.of(
            6, 2_000_000_000,
            5, 1_500_000,
            4, 50_000,
            3, 5_000,
            BONUS_MATCH_RANK, 30_000_000
    );

    private List<Lotto> purchasedLottos = new ArrayList<>();
    private Lotto winningLotto;
    private int bonusNumber;

    public static void main(String[] args) {
        Application app = new Application();
        app.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        try {
            int purchaseAmount = getPurchaseAmount(scanner);
            int ticketCount = purchaseAmount / LOTTO_PRICE;
            generateLottos(ticketCount);

            System.out.printf("%d개를 구매했습니다.%n", ticketCount);
            purchasedLottos.forEach(System.out::println);

            winningLotto = new Lotto(getWinningNumbers(scanner));
            bonusNumber = getBonusNumber(scanner);

            Map<Integer, Integer> result = checkResults();
            printResults(result, purchaseAmount);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private int getPurchaseAmount(Scanner scanner) {
        System.out.println("구입금액을 입력해 주세요.");
        int amount = Integer.parseInt(scanner.nextLine());
        if (amount % LOTTO_PRICE != 0 || amount <= 0) {
            throw new IllegalArgumentException("[ERROR] 구입 금액은 1,000원 단위로 입력해야 합니다.");
        }
        return amount;
    }

    private void generateLottos(int count) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            Set<Integer> numbers = new HashSet<>();
            while (numbers.size() < Lotto.LOTTO_NUMBER_COUNT) {
                numbers.add(random.nextInt(Lotto.MAX_NUMBER) + Lotto.MIN_NUMBER);
            }
            purchasedLottos.add(new Lotto(new ArrayList<>(numbers)));
        }
    }

    private List<Integer> getWinningNumbers(Scanner scanner) {
        System.out.println("당첨 번호를 입력해 주세요.");
        return Arrays.stream(scanner.nextLine().split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private int getBonusNumber(Scanner scanner) {
        System.out.println("보너스 번호를 입력해 주세요.");
        int number = Integer.parseInt(scanner.nextLine().trim());
        if (number < Lotto.MIN_NUMBER || number > Lotto.MAX_NUMBER) {
            throw new IllegalArgumentException("[ERROR] 보너스 번호는 1부터 45 사이의 숫자여야 합니다.");
        }
        return number;
    }

    private Map<Integer, Integer> checkResults() {
        Map<Integer, Integer> results = new TreeMap<>(Collections.reverseOrder());
        for (Lotto lotto : purchasedLottos) {
            int matchCount = lotto.countMatchingNumbers(winningLotto);
            if (matchCount == 6) {
                results.put(6, results.getOrDefault(6, 0) + 1);
            } else if (matchCount == 5 && lotto.containsBonusNumber(bonusNumber)) {
                results.put(BONUS_MATCH_RANK, results.getOrDefault(BONUS_MATCH_RANK, 0) + 1);
            } else if (matchCount >= 3) {
                results.put(matchCount, results.getOrDefault(matchCount, 0) + 1);
            }
        }
        return results;
    }

    private void printResults(Map<Integer, Integer> results, int purchaseAmount) {
        System.out.println("당첨 통계\n---");
        int totalPrize = 0;

        for (Map.Entry<Integer, Integer> entry : PRIZES.entrySet()) {
            int count = results.getOrDefault(entry.getKey(), 0);
            int prize = entry.getValue();
            System.out.printf("%d개 일치 (%d원) - %d개%n", entry.getKey(), prize, count);
            totalPrize += count * prize;
        }

        double profitRate = (double) totalPrize / purchaseAmount * 100;
        System.out.printf("총 수익률은 %.1f%%입니다.%n", profitRate);
    }
}