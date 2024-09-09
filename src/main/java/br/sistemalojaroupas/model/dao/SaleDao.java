package br.sistemalojaroupas.model.dao;

import br.sistemalojaroupas.db.DB;
import br.sistemalojaroupas.model.entities.Sale;
import br.sistemalojaroupas.model.entities.util.CodeGenerator;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

public class SaleDao {

    private static final String MOMENT_FIELD = "moment"; // Definindo a constante para "moment"
    
    private static ObjectRepository<Sale> repSale;
    private static ObjectRepository<CodeGenerator> repCode;
    private static CodeGenerator codeGenerator;

    // Construtor privado para ocultar o público implícito
    private SaleDao() {
        // Impedir instanciamento
    }

    static {
        repCode = DB.getDB().getRepository(CodeGenerator.class);

        if (!DB.getDB().hasRepository(Sale.class)) {
            codeGenerator = new CodeGenerator(Sale.class.getSimpleName(), 0L);
            repCode.insert(codeGenerator);
        } else {
            codeGenerator = repCode.find(ObjectFilters.eq("classType", Sale.class.getSimpleName()))
                    .firstOrDefault();
        }
        repSale = DB.getDB().getRepository(Sale.class);
    }

    public static void insert(Sale sale) {
        codeGenerator.setLastCode(codeGenerator.getLastCode() + 1);
        sale.setId(codeGenerator.getLastCode());
        repSale.insert(sale);
        repCode.update(codeGenerator);
    }

    public static List<Sale> findAll() {
        // Retorno imediato sem atribuir à variável temporária 'list'
        return repSale.find(FindOptions.sort(MOMENT_FIELD, SortOrder.Descending)).toList();
    }

    public static Sale findById(Long id) {
        // Retorno imediato sem atribuir à variável temporária 'sale'
        return repSale.find(ObjectFilters.eq("id", id)).firstOrDefault();
    }

    public static void update(Sale sale) {
        repSale.update(sale);
    }

    public static void remove(Sale sale) {
        repSale.remove(sale);
    }

    public static void removeById(Long id) {
        Sale sale = findById(id);
        repSale.remove(sale);
    }

    public static Long size() {
        return repSale.size();
    }

    public static Double revenues(Integer lastDays) {
        Double revenues = 0.0;
        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        List<Sale> sales;

        if (lastDays == 0) {
            sales = findAll();
        } else {
            cal.add(Calendar.DAY_OF_MONTH, -lastDays);
            sales = repSale.find(ObjectFilters.gte(MOMENT_FIELD, cal.getTime())).toList();
        }

        for (Sale obj : sales) {
            revenues += obj.getTotal();
        }

        return revenues;
    }

    public static List<Sale> filterByPeriod(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("As datas não podem ser nulas.");
        }
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("A data inicial não pode ser depois da data final.");
        }

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(end);
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        end = calEnd.getTime();

        // Retorno imediato sem atribuir à variável temporária 'sales'
        return repSale.find(ObjectFilters.and(
                ObjectFilters.gte(MOMENT_FIELD, start),
                ObjectFilters.lte(MOMENT_FIELD, end)),
                FindOptions.sort(MOMENT_FIELD, SortOrder.Descending)).toList();
    }
}
