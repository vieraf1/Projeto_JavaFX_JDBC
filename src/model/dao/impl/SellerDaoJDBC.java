package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;

import db.DB;
import db.DBException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {
	
	private static final String QUERY_FIND_BY_ID = 
			"SELECT seller.*,department.Name as DepName  "
			+ "FROM seller "
			+ "INNER JOIN department ON seller.DepartmentId = department.Id "
			+ "WHERE seller.Id = ?";
	
	private static final String QUERY_FIND_BY_DEPARTMENT = 
			"SELECT seller.*,department.Name as DepName "
			+ "FROM seller "
			+ "INNER JOIN department ON seller.DepartmentId = department.Id "
			+ "WHERE DepartmentId = ? "
			+ "ORDER BY Name";
	
	private static final String QUERY_FIND_ALL = 
			"SELECT seller.*,department.Name as DepName "
			+ "FROM seller "
			+ "INNER JOIN department ON seller.DepartmentId = department.Id "
			+ "ORDER BY Name";
	
	private static final String QUERY_INSERT = 
			"INSERT INTO seller (Name, Email, BirthDate, BaseSalary, DepartmentId) "
			+ "VALUES  (?, ?, ?, ?, ?)";
	
	private static final String QUERY_UPDATE = 
			"UPDATE seller "
			+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
			+ "WHERE Id = ?";
	
	private static final String QUERY_DELETE = 
			"DELETE FROM seller "
			+ "WHERE Id = ?";
	
	private Connection con;
	
	public SellerDaoJDBC(Connection con) {
		this.con = con;
	}

	@Override
	public void insert(Seller obj) {
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			
			int rowsInserted = st.executeUpdate();
			
			if(rowsInserted > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
				}
				DB.closeResultSet(rs);
			} else {
				throw new DBException("Erro inesperado! Nenhuma registro foi inserido no banco");
			}
					
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Seller obj) {
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(QUERY_UPDATE);
			
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			st.setInt(6, obj.getId());
			
			st.executeUpdate();
					
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = con.prepareStatement(QUERY_DELETE);
			
			st.setInt(1, id);
			
			int r = st.executeUpdate();
			if(r == 0) {
				System.out.println("Não existe seller com este ID!");
			} else {
				System.out.println("Deletado com sucesso!");
			}
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public Seller findById(Integer id) {
		
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			st = con.prepareStatement(QUERY_FIND_BY_ID);
			
			st.setInt(1, id);
			rs = st.executeQuery();
			
			if(rs.next()) {
				Department dep = instantiateDeparment(rs);
				Seller seller = instantiateSeller(rs, dep);
				return seller;
			}
			
			return null;
			
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			st = con.prepareStatement(QUERY_FIND_ALL);
			
			rs = st.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while (rs.next()) {
				
				Department dep = map.get(rs.getInt("DepartmentId"));
				if(dep == null) {
					dep = instantiateDeparment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				
				Seller seller = instantiateSeller(rs, dep);
				list.add(seller);
			}
			
			return list;
			
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}
	
	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			st = con.prepareStatement(QUERY_FIND_BY_DEPARTMENT);
			
			st.setInt(1, department.getId());
			rs = st.executeQuery();
			
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while (rs.next()) {
				
				Department dep = map.get(rs.getInt("DepartmentId"));
				if(dep == null) {
					dep = instantiateDeparment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				
				Seller seller = instantiateSeller(rs, dep);
				list.add(seller);
			}
			
			return list;
			
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}
	
	private Department instantiateDeparment(ResultSet rs) throws SQLException {
		return new Department(rs.getInt("DepartmentId"), rs.getString("DepName"));
	}
	
	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException {
		return new Seller(
				          rs.getInt("Id"), 
				          rs.getString("Name"), 
				          rs.getString("Email"), 
				          new java.util.Date(rs.getTimestamp("BirthDate").getTime()), 
				          rs.getDouble("BaseSalary"), 
				          dep);
	}

}
