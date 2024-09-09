package br.sistemalojaroupas.model.dao;

import br.sistemalojaroupas.db.DB;
import br.sistemalojaroupas.db.DBException;
import br.sistemalojaroupas.model.dao.exceptions.LoginException;
import br.sistemalojaroupas.model.entities.Employee;
import br.sistemalojaroupas.model.entities.User;
import java.util.List;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

public class UserDao {

    private static final ObjectRepository<User> repUser;

    // Fieldname should be used consistently instead of hardcoding "userName"
    private static final String fieldName = "userName";

    private UserDao() {
        throw new UnsupportedOperationException("Esta é uma classe utilitária e não pode ser instanciada.");
    }

    static {
        if (!DB.getDB().hasRepository(User.class)) {
            repUser = DB.getDB().getRepository(User.class);
            
            User u = new User();
            u.setUserName("admin");
            u.setPassword("admin");
            
            repUser.insert(u);
        } else {
            repUser = DB.getDB().getRepository(User.class);
        }
    }

    public static ObjectRepository<User> getRepUser() {
        return repUser;
    }

    public static User verify(String userName, String password) {
        User u = UserDao.findByUser(userName);
        
        if (userName.equals("") || password.equals("")) throw new LoginException("Preencha todos os campos.");
        if (u == null) throw new LoginException("Usuário ou senha inválidos.");
        if (!u.getUserName().equals(userName) || !u.getPassword().equals(password)) {
            throw new LoginException("Usuário ou senha inválidos.");
        }
        return u;
    }

    public static void insert(User u) {
        repUser.insert(u);
    }

    public static void update(User user) {
        User temp = repUser.find(ObjectFilters.eq(fieldName, user.getUserName()))
                .firstOrDefault();
        if (user.equals(temp) || temp == null) {
            repUser.update(user); // Corrigido para 'user'
        } else {
            throw new DBException("Nome de usuário já existe.");
        }
    }

    public static List<User> findAll() {
        FindOptions findOptions = FindOptions.sort(fieldName, SortOrder.Ascending);
        return repUser.find(findOptions).toList();
    }

    public static User findById(NitriteId id) {
        return repUser.getById(id);
    }

    public static User findByUser(String userName) {
        return repUser.find(ObjectFilters.eq(fieldName, userName)).firstOrDefault();
    }

    public static User findByEmployee(Employee employee) {
        return repUser.find(ObjectFilters.eq("employee.id", employee.getId())).firstOrDefault();
    }

    public static void remove(User u) {
        repUser.remove(u);
    }
}
