package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlUpdate;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="_actions_phenomenon", catalog="gis", schema="public")
public class ActionsPhenomenon extends Model{
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="_actions_phenomenon_seq")

    private int id;
    private String title;
    private String agency;

    public ActionsPhenomenon() {}

    public ActionsPhenomenon(String title, String agency){
        this.title = title;
        this.agency = agency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public static Finder<Integer, ActionsPhenomenon> find = new Finder<Integer, ActionsPhenomenon>(ActionsPhenomenon.class);

    public static List<ActionsPhenomenon> all() { return find.all(); }

    public static void add(String item, String agency) {
        SqlQuery select = Ebean.createSqlQuery("SELECT * FROM _actions_phenomenon WHERE title='"+item+"'");

        if(select.findList().size() == 0 ) {
            SqlUpdate insert = Ebean.createSqlUpdate("INSERT INTO _actions_phenomenon (title, agency) VALUES ('" + item + "','" + agency + "')");
            insert.execute();
        }
    }

    public static void delete(Integer id){
        String phenomenon = ActionsPhenomenon.find.where().eq("id", id).findUnique().getTitle();

        find.ref(id).delete();

        SqlUpdate deleteAction = Ebean.createSqlUpdate("DELETE FROM _actions WHERE phenomenon = '"+ phenomenon +"'");
        deleteAction.execute();
    }

    public static void update(ActionsPhenomenon item){
        String phenomenon = ActionsPhenomenon.find.where().eq("id", item.getId()).findUnique().getTitle();
        System.out.println(phenomenon);
        System.out.println(item.getTitle());
        item.update();

        SqlUpdate updateAction = Ebean.createSqlUpdate("UPDATE _actions SET phenomenon = '"+ item.getTitle() +"' WHERE phenomenon = '"+ phenomenon +"'");
        updateAction.execute();
    }
}
