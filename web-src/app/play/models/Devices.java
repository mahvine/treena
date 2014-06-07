package play.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class Devices extends Model{
	
	@Id
	public long id;
	public String deviceIdentifier;
	public double longitude = 120.994964;
	public double latitude = 14.563633;
	
	@ManyToMany(cascade=CascadeType.ALL)  
    @JoinTable(name="discovered_trees",  
    joinColumns={@JoinColumn(name="device_id", referencedColumnName="id")},  
    inverseJoinColumns={@JoinColumn(name="golden_tree_id", referencedColumnName="id")})
	public List<GoldenTrees> discoveredTrees;

	@OneToMany(mappedBy="devices")
	public List<PlantedTrees> plantedTrees;

	
	public static Finder<Long,Devices> find = new Finder<Long, Devices>(Long.class,Devices.class);


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((deviceIdentifier == null) ? 0 : deviceIdentifier.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Devices other = (Devices) obj;
		if (deviceIdentifier == null) {
			if (other.deviceIdentifier != null)
				return false;
		} else if (!deviceIdentifier.equals(other.deviceIdentifier))
			return false;
		if (id != other.id)
			return false;
		return true;
	}

	
	
	

}
