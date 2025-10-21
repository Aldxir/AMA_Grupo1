package com.example.alden.services

import com.example.alden.data.AccessRepository
import com.example.alden.models.RegistroAcceso
import com.example.alden.rules.canRegister

object AttendanceService {
    /**
     * Evalua la politica canRegister. Si el usuario esta habilitado, guarda y retorna true.
     * Si no, no guarda y retorna false.
     */
    fun registrarSiHabilitado(reg: RegistroAcceso): Boolean {
        val ok = canRegister(reg)
        if (ok) AccessRepository.add(reg)

        return ok
    }
}