package com.agroberriesmx.agrokiosko.data.network.response

import android.util.Log
import com.agroberriesmx.agrokiosko.domain.model.ActivitiesModel
import com.google.gson.annotations.SerializedName

data class ActivitiesResponse (
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("response") val activities: List<ActivitiesResponseItem>
)

data class ActivitiesResponseItem (
    @SerializedName("cCodigoLab") val cCodigoLab:String,
    @SerializedName("cTipoLab") val cTipoLab:String,
    @SerializedName("cCodigoSem") val cCodigoSem:String,
    @SerializedName("dDocumentoLab") val dDocumentoLab:String,
    @SerializedName("cCodigoLug") val cCodigoLug:String ,
    @SerializedName("cCodigoTra") val cCodigoTra:String ,
    @SerializedName("cCodigoLot") val cCodigoLot:String ,
    @SerializedName("vNombreLot") val vNombreLot:String ,
    @SerializedName("nSecuenciaLab") val nSecuenciaLab:String,
    @SerializedName("cCodigoAct") val cCodigoAct:String ,
    @SerializedName("vNombreAct") val vNombreAct:String ,
    @SerializedName("cCodigoCul") val cCodigoCul:String ,
    @SerializedName("vNombreCul") val vNombreCul:String ,
    @SerializedName("nCantidadLab") val nCantidadLab:String ,
    @SerializedName("nCostoLab") val nCostoLab:String ,
    @SerializedName("nCostonorLab") val nCostonorLab:String ,
    @SerializedName("nCantextLab") val nCantextLab:String ,
    @SerializedName("nCostoextLab") val nCostoextLab:String ,
    @SerializedName("nCanttripleLab") val nCanttripleLab:String ,
    @SerializedName("nCostotripleLab") val nCostotripleLab:String ,
    @SerializedName("nCostoopeLab") val nCostoopeLab:String ,
    @SerializedName("nSueldoLab") val nSueldoLab:String ,
    @SerializedName("vNombreTra") val vNombreTra:String ,
    @SerializedName("vApellidopatTra") val vApellidopatTra:String ,
    @SerializedName("vApellidomatTra") val vApellidomatTra:String ,
    @SerializedName("abreviadoTam") val abreviadoTam:String? ,
    @SerializedName("vEmailTra") val vEmailTra:String,
    @SerializedName("descuentoSindicato") val descuentoSindicato: Double?,
    @SerializedName("esSindicalizado") val esSindicalizado: Boolean?
) {
    fun toDomain(): ActivitiesModel{
        Log.e("Deserialization_Check", "En toDomain(): abreviadoTam es -> '${this.abreviadoTam}'")
        return ActivitiesModel(
            cCodigoLab = cCodigoLab,
            cTipoLab = cTipoLab,
            cCodigoSem = cCodigoSem,
            dDocumentoLab = dDocumentoLab,
            cCodigoLug = cCodigoLug,
            cCodigoTra = cCodigoTra,
            cCodigoLot = cCodigoLot,
            vNombreLot = vNombreLot,
            nSecuenciaLab = nSecuenciaLab,
            cCodigoAct = cCodigoAct,
            vNombreAct = vNombreAct,
            cCodigoCul = cCodigoCul,
            vNombreCul = vNombreCul,
            nCantidadLab = nCantidadLab,
            nCostoLab = nCostoLab,
            nCostonorLab = nCostonorLab,
            nCantextLab = nCantextLab,
            nCostoextLab = nCostoextLab,
            nCanttripleLab = nCanttripleLab,
            nCostotripleLab = nCostotripleLab,
            nCostoopeLab = nCostoopeLab,
            nSueldoLab = nSueldoLab,
            vNombreTra = vNombreTra,
            vApellidopatTra = vApellidopatTra,
            vApellidomatTra = vApellidomatTra,
            abreviadoTam = abreviadoTam,
            vEmailTra = vEmailTra,
            descuentoSindicato = descuentoSindicato,
            esSindicalizado = this.esSindicalizado ?: false
        )
    }
}