package application.data.model;

public class DatabaseObject {
    private Integer id = -1;

    public DatabaseObject() {
    }

    public DatabaseObject(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void save() {
        try {
            new DatabaseAction<>().save(this, (DatabaseLink) DatabaseLink.getLinkClass(this.getClass()).newInstance());
        } catch (NullPointerException | InstantiationException | IllegalAccessException ex) {
            application.error.Error.DATABASE_SAVE_CLASS_INIT.record().create(ex);
        }
    }
}
