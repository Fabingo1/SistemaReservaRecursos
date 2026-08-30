package dominio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Reserva {
    private String id;
    private String actividad;
    private Date fecha;
    private Date horaInicio;
    private Date horaFin;
    private EstadoReserva estado;
    private Funcionario funcionario;
    private List<DetalleReserva> detalles;

    public Reserva() {
        this.detalles = new ArrayList<>();
        this.estado = EstadoReserva.ACTIVA;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Date horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Date getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Date horaFin) {
        this.horaFin = horaFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public List<DetalleReserva> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleReserva> detalles) {
        this.detalles = detalles;
    }


    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    public boolean estaActiva() {
        return this.estado == EstadoReserva.ACTIVA;
    }

    public boolean perteneceA(Funcionario funcionario) {
        return this.funcionario != null
                && funcionario != null
                && this.funcionario.getId().equals(funcionario.getId());
    }
}
