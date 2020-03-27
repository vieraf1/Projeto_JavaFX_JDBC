package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.DB;
import db.DBException;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentDaoJDBC implements DepartmentDao {
	
	private static final String QUERY_FIND_BY_ID = 
			"SELECT *  "
			+ "FROM department "
			+ "WHERE Id = ?";
	
	private static final String QUERY_FIND_ALL = 
			"SELECT * "
			+ "FROM department ";
	
	private static final String QUERY_INSERT = 
			"INSERT INTO department (Name) "
			+ "VALUES  (?)";
	
	private static final String QUERY_UPDATE = 
			"UPDATE department "
			+ "SET Name = ? "
			+ "WHERE Id = ?";
	
	private static final String QUERY_DELETE = 
			"DELETE FROM department "
			+ "WHERE Id = ?";
	
	private Connection con;

	public DepartmentDaoJDBC(Connection con) {
		this.con = con;
	}
	
	@Override
	public void insert(Department obj) {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(QUERY_INSERT, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, obj.getName());
			
			int rows = ps.executeUpdate();
			
			if(rows > 0) {
				ResultSet rs = ps.getGeneratedKeys();
				if(rs.next()) {
					Integer id = rs.getInt(1);
					obj.setId(id);
					System.out.println("Inserido com sucesso! Id = " + id);
				}
				DB.closeResultSet(rs);
			}
			else {
				throw new DBException("Erro! não foi inserido nenhum departamento");
			}
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}
	}

	@Override
	public void update(Department obj) {
		PreparedStatement ps = null;	
		try {
			ps = con.prepareStatement(QUERY_UPDATE);
			
			ps.setString(1, obj.getName());
			ps.setInt(2, obj.getId());
			
			ps.executeUpdate();
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement ps = null;	
		try {
			ps = con.prepareStatement(QUERY_DELETE);
			
			ps.setInt(1, id);
			
			int rowsAffected = ps.executeUpdate();
			
			if(rowsAffected > 0) {
				System.out.println("Departamento excluído com sucesso!");
			} else {
				System.out.println("Não existe departamento com este ID!");
			}
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(ps);
		}
	}

	@Override
	public Department findById(Integer id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(QUERY_FIND_BY_ID);
			
			ps.setInt(1, id);
			
			rs = ps.executeQuery();
			
			if(rs.next()) {
				return instantiateDeparment(rs);
			}else {
				System.out.println("Não existe Departamento para o ID escolhido!");
				return null;
			}
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(ps);
		}
	}

	@Override
	public List<Department> findAll() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Department> lista = new ArrayList<>();
		try {
			ps = con.prepareStatement(QUERY_FIND_ALL);
			
			rs = ps.executeQuery();
			
			while(rs.next()) {
				lista.add(instantiateDeparment(rs));
			}
			return lista;
		} catch(SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(ps);
		}
	}
	
	private Department instantiateDeparment(ResultSet rs) throws SQLException {
		return new Department(rs.getInt("Id"), rs.getString("Name"));
	}
}