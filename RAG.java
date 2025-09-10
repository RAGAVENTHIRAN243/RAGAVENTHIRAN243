import java.util.*;
import java.time.*;

// ----------------------- TariffPlan & Subclasses -----------------------
abstract class TariffPlan {
    String planName;

    public TariffPlan(String planName) {
        this.planName = planName;
    }

    public abstract double calculateCharge(int units);

    public double calculateCharge(int units, boolean isPeakHour) {
        double charge = calculateCharge(units);
        return isPeakHour ? charge * 1.2 : charge; // surcharge for peak hour
    }

    public String getPlanName() {
        return planName;
    }
}

class DomesticTariff extends TariffPlan {
    public DomesticTariff() {
        super("Domestic");
    }

    @Override
    public double calculateCharge(int units) {
        if (units <= 100) return units * 1.5;
        else if (units <= 300) return 100 * 1.5 + (units - 100) * 2.5;
        else return 100 * 1.5 + 200 * 2.5 + (units - 300) * 4.0;
    }
}

class CommercialTariff extends TariffPlan {
    public CommercialTariff() {
        super("Commercial");
    }

    @Override
    public double calculateCharge(int units) {
        if (units <= 100) return units * 3.0;
        else if (units <= 300) return 100 * 3.0 + (units - 100) * 5.0;
        else return 100 * 3.0 + 200 * 5.0 + (units - 300) * 7.0;
    }
}

// ---------------------------- Consumer ----------------------------
class Consumer {
    private static int idCounter = 1000;
    private int consumerId;
    private String name;
    private String address;
    private TariffPlan tariffPlan;
    private String status; // Active/Inactive

    public Consumer(String name, String address, TariffPlan tariffPlan) {
        this.consumerId = idCounter++;
        this.name = name;
        this.address = address;
        this.tariffPlan = tariffPlan;
        this.status = "Active";
    }

    public int getConsumerId() { return consumerId; }
    public String getName() { return name; }
    public TariffPlan getTariffPlan() { return tariffPlan; }
    public String getStatus() { return status; }

    public void deactivate() { this.status = "Inactive"; }

    @Override
    public String toString() {
        return consumerId + " - " + name + " (" + tariffPlan.getPlanName() + ")";
    }
}

// ---------------------------- Meter ----------------------------
class Meter {
    private static int meterCounter = 5000;
    private int meterId;
    private Consumer consumer;
    private int lastReading;
    private LocalDate lastReadingDate;
    private String health; // Good/Needs Maintenance

    public Meter(Consumer consumer) {
        this.meterId = meterCounter++;
        this.consumer = consumer;
        this.lastReading = 0;
        this.lastReadingDate = LocalDate.now().minusMonths(1);
        this.health = "Good";
    }

    public int getMeterId() { return meterId; }
    public Consumer getConsumer() { return consumer; }
    public int getLastReading() { return lastReading; }

    public void recordReading(int newReading) {
        if (newReading < lastReading) {
            System.out.println("Invalid reading. Must be >= last reading.");
            return;
        }
        lastReading = newReading;
        lastReadingDate = LocalDate.now();
    }

    public String getHealth() { return health; }

    public void setHealth(String health) { this.health = health; }

    @Override
    public String toString() {
        return "Meter " + meterId + " [" + consumer + "]";
    }
}

// ---------------------------- Bill ----------------------------
class Bill {
    private static int billCounter = 2000;
    private int billNo;
    private Consumer consumer;
    private int units;
    private double amount;
    private LocalDate dueDate;
    private String state; // Unpaid/Paid/Late

    public Bill(Consumer consumer, int units, double amount) {
        this.billNo = billCounter++;
        this.consumer = consumer;
        this.units = units;
        this.amount = amount;
        this.dueDate = LocalDate.now().plusDays(15);
        this.state = "Unpaid";
    }

    public int getBillNo() { return billNo; }
    public String getState() { return state; }
    public double getAmount() { return amount; }

    public void recordPayment(double payment) {
        if (payment >= amount) {
            this.state = "Paid";
        } else {
            System.out.println("Partial payment. Bill still unpaid.");
        }
    }

    public void applySurcharge() {
        if (state.equals("Unpaid") && LocalDate.now().isAfter(dueDate)) {
            amount += 50; // Late fee
            state = "Late";
        }
    }

    @Override
    public String toString() {
        return "Bill #" + billNo + " | " + consumer.getName() + " | Units: " + units + " | Amount: ₹" + amount + " | Status: " + state;
    }
}

// ------------------------ UtilityService ------------------------
class UtilityService {
    private List<Consumer> consumers = new ArrayList<>();
    private List<Meter> meters = new ArrayList<>();
    private List<Bill> bills = new ArrayList<>();

    public Consumer registerConsumer(String name, String address, TariffPlan plan) {
        Consumer c = new Consumer(name, address, plan);
        consumers.add(c);
        return c;
    }

    public Meter installMeter(Consumer consumer) {
        Meter m = new Meter(consumer);
        meters.add(m);
        return m;
    }

    public Bill generateBill(Meter meter, int newReading) {
        int consumed = newReading - meter.getLastReading();
        meter.recordReading(newReading);

        double amount = meter.getConsumer().getTariffPlan().calculateCharge(consumed);
        Bill bill = new Bill(meter.getConsumer(), consumed, amount);
        bills.add(bill);
        return bill;
    }

    public void postPayment(int billNo, double amount) {
        for (Bill b : bills) {
            if (b.getBillNo() == billNo) {
                b.recordPayment(amount);
                return;
            }
        }
        System.out.println("Bill not found.");
    }

    public void applyDunning() {
        for (Bill b : bills) {
            b.applySurcharge();
        }
    }

    public void printAgingReport() {
        System.out.println("---- Aging Report ----");
        for (Bill b : bills) {
            if (!b.getState().equals("Paid")) {
                System.out.println(b);
            }
        }
    }

    public void revenueByTariffType() {
        Map<String, Double> revenue = new HashMap<>();
        for (Bill b : bills) {
            if (b.getState().equals("Paid") || b.getState().equals("Late")) {
                String type = b.consumer.getTariffPlan().getPlanName();
                revenue.put(type, revenue.getOrDefault(type, 0.0) + b.getAmount());
            }
        }
        System.out.println("---- Revenue by Tariff Type ----");
        for (String type : revenue.keySet()) {
            System.out.println(type + ": ₹" + revenue.get(type));
        }
    }
}

// ---------------------------- Main Class ----------------------------
public class UtilityAppMain {
    public static void main(String[] args) {
        UtilityService service = new UtilityService();

        // Register Consumers
        Consumer c1 = service.registerConsumer("Alice", "City A", new DomesticTariff());
        Consumer c2 = service.registerConsumer("BobCorp", "City B", new CommercialTariff());

        // Install Meters
        Meter m1 = service.installMeter(c1);
        Meter m2 = service.installMeter(c2);

        // Record Readings and Generate Bills
        Bill b1 = service.generateBill(m1, 250); // Consumed: 250
        Bill b2 = service.generateBill(m2, 450); // Consumed: 450

        System.out.println(b1);
        System.out.println(b2);

        // Post Payments
        service.postPayment(b1.getBillNo(), b1.getAmount());

        // Apply Dunning (Late Fees)
        service.applyDunning();

        // Print Reports
        service.printAgingReport();
        service.revenueByTariffType();
    }
}
