package meerkated.meerkated_tree.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "left_key")
    private Long leftKey;
    
    @Column(name = "right_key")
    private Long rightKey;
    
    @Column(name = "insertion_level")
    private Long insertionLevel;
    
    public Long getInsertionLevel() {
        return insertionLevel;
    }
    
    public void setInsertionLevel(Long insertionLevel) {
        this.insertionLevel = insertionLevel;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Long getLeftKey() {
        return leftKey;
    }
    
    public void setLeftKey(Long leftKey) {
        this.leftKey = leftKey;
    }
    
    public Long getRightKey() {
        return rightKey;
    }
    
    public void setRightKey(Long rightKey) {
        this.rightKey = rightKey;
    }
}
