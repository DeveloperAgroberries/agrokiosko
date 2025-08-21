package com.agroberriesmx.agrokiosko.data.network.response

import com.agroberriesmx.agrokiosko.domain.model.PayrollModel
import com.google.gson.annotations.SerializedName

data class PayrollResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("response") val payroll: List<PayrollResponseItem>
)

data class PayrollResponseItem (
    @SerializedName("cCodigoTra") val cCodigoTra:String,
    @SerializedName("cCodigoSem") val cCodigoSem:String,
    @SerializedName("nCantidadLab") val nCantidadLab:String,
    @SerializedName("nCostoLab") val nCostoLab:String,
    @SerializedName("nCostonorLab") val nCostonorLab:String,
    @SerializedName("nCantextLab") val nCantextLab:String,
    @SerializedName("nCostoextLab") val nCostoextLab:String,
    @SerializedName("nCanttripleLab") val nCanttripleLab:String,
    @SerializedName("nCostotripleLab") val nCostotripleLab:String,
    @SerializedName("nCostoopeLab") val nCostoopeLab:String,
    @SerializedName("nSueldoLab") val nSueldoLab:String,
    @SerializedName("cTipoLab") val cTipoLab:String,
    @SerializedName("vNombreTra") val vNombreTra:String,
    @SerializedName("vApellidopatTra") val vApellidopatTra:String,
    @SerializedName("vApellidomatTra") val vApellidomatTra:String,
    @SerializedName("vEmailTra") val vEmailTra:String,
    @SerializedName("vNombreTrt") val vNombreTrt:String,
    @SerializedName("vNombreCam") val vNombreCam:String
) {
    fun toDomain(): PayrollModel{
        return PayrollModel(
            cCodigoTra = cCodigoTra,
            cCodigoSem  = cCodigoSem,
            nCantidadLab = nCantidadLab,
            nCostoLab = nCostoLab,
            nCostonorLab = nCostonorLab,
            nCantextLab = nCantextLab,
            nCostoextLab = nCostoextLab,
            nCanttripleLab = nCanttripleLab,
            nCostotripleLab = nCostotripleLab,
            nCostoopeLab = nCostoopeLab,
            nSueldoLab = nSueldoLab,
            cTipoLab = cTipoLab,
            vNombreTra = vNombreTra,
            vApellidopatTra = vApellidopatTra,
            vApellidomatTra = vApellidomatTra,
            vEmailTra = vEmailTra,
            vNombreTrt = vNombreTrt,
            vNombreCam = vNombreCam
        )
    }
}